package uk.co.dalelane.kafkastreams.xboxlive.streams.matching;

import java.time.format.DateTimeParseException;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.dalelane.kafkastreams.xboxlive.data.games.TitleIdInfo;
import uk.co.dalelane.kafkastreams.xboxlive.data.presence.EnrichedPresence;
import uk.co.dalelane.kafkastreams.xboxlive.data.serdes.XboxSerdes;
import uk.co.dalelane.kafkastreams.xboxlive.data.sessions.PlaySession;


/**
 * Example of using Kafka Streams to emit complex events recognised
 *  from a stream of raw, low-level events.
 *
 *  Use case: Turning a stream of low-level events describing when
 *   users start and stop doing things into a joined up stream of
 *   completed activities.
 *
 *  Background:
 *   The "presence" stream of events records when a gamer starts
 *   doing something (e.g. starts playing a game) and when they go
 *   offline.
 *
 *   This stream processor pairs start and stop events to produce
 *   a stream of logical events representing a "play session" - an
 *   activity with a defined start and end time.
 *
 *   If a gamer goes straight from one game to another, this results
 *   in two play sessions - with the event for starting to play the
 *   second game becoming the end event for the first play session
 *   and the start event for the second play session.
 *
 *   This is a good example of using Kafka Streams to turn a stream
 *   of low-level events into a higher-level stream of logical
 *   events, by looking at the events in the context of events that
 *   come before them.
 *
 *
 *  TOPICS:
 *   Input:     XBOX.PRESENCE.ENRICHED
 *   Output:    XBOX.PLAYSESSIONS
 *
 */
public class PlaySessionGenerator {

    private static Logger log = LoggerFactory.getLogger(PlaySessionGenerator.class);

    private static final String INPUT_TOPIC  = "XBOX.PRESENCE.ENRICHED";
    private static final String OUTPUT_TOPIC = "XBOX.PLAYSESSIONS";

    private static final String STATE_STORE_NAME = PlaySessionGenerator.class.getCanonicalName();


    public static void create(final StreamsBuilder builder) {
        log.info("Creating PlaySessionGenerator");

        // This is a stateful stream processor, so that events
        //  can be compared with previous events. This means
        //  we need to create a persistent store to maintain state.
        builder.addStateStore(
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(STATE_STORE_NAME),
                XboxSerdes.GAMERTAG_SERDES,
                XboxSerdes.PLAY_SESSION_SERDES));

        // we're only emitting play sessions for games, so this
        //  filters out completed sessions for using a media player or app
        final Predicate<String, PlaySession> filterOutNonGameSessions = new Predicate<>() {
            @Override
            public boolean test(String key, PlaySession value) {
                return TitleIdInfo.GAME.equals(TitleIdInfo.getTitleType(value.getTitleId()));
            }
        };


        builder
            // input the enriched presences (with additional info about users)
            .stream(INPUT_TOPIC, Consumed.with(XboxSerdes.GAMERTAG_SERDES,
                                               XboxSerdes.ENRICHED_PRESENCE_SERDES))
            // pair the raw events into logical events representing completed play sessions
            .process(new PresenceProcessorSupplier(),
                     Named.as("pair_start_and_stop_presences"),
                     STATE_STORE_NAME)
            // filter out completed sessions using media players or other non-game apps
            .filter(filterOutNonGameSessions,
                    Named.as("filter_out_non_game_sessions"))
            // output to a topic for logical play session events
            .to(OUTPUT_TOPIC, Produced.with(XboxSerdes.GAMERTAG_SERDES,
                                            XboxSerdes.PLAY_SESSION_SERDES));
    }




    static class PresenceProcessorSupplier implements ProcessorSupplier<String, EnrichedPresence, String, PlaySession> {
        @Override
        public Processor<String, EnrichedPresence, String, PlaySession> get() {

            return new ContextualProcessor<String, EnrichedPresence, String, PlaySession>() {
                private KeyValueStore<String, PlaySession> openSessions;

                @Override
                public void init(ProcessorContext<String, PlaySession> context) {
                    super.init(context);
                    openSessions = context.getStateStore(STATE_STORE_NAME);
                }


                @Override
                public void process(Record<String, EnrichedPresence> record) {
                    String key = record.key();
                    EnrichedPresence value = record.value();

                    try {
                        PlaySession openSession = openSessions.get(key);
                        if (openSession == null) {
                            // there is no current known play session for this user

                            if (value.userOffline()) {
                                // there is no current play session, but this event
                                //  says the user has gone offline, so we ignore it
                            }
                            else {
                                // this is the start of a new play session
                                openSessions.put(key, PlaySession.createFromStart(value));
                            }

                            // no current play session, so nothing to return yet
                        }
                        else {

                            // there is a current play session - mark it as complete
                            openSession.setEnd(value.getDate());


                            if (value.userOffline()) {
                                // this is the end of the current play session
                                //  so need to remove it from the state by setting it to null
                                openSessions.put(key, null);
                            }
                            else if (value.getTitleId().equals(openSession.getTitleId())) {
                                // looks like a duplicate event - ignore
                                return;
                            }
                            else {
                                // the user has switched from one game to another
                                //  so we're starting a new play session
                                openSessions.put(key, PlaySession.createFromStart(value));
                            }

                            // propogate a completed play session
                            if (openSession.getDuration().toHours() > 8) {
                                // assume this is an error caused by missing events,
                                //  as surely noone plays for longer than eight
                                //  hours without a break!
                                log.error("Invalid play session detected {}", openSession);
                            }
                            else {
                                // looks legit
                                context().forward(record.withValue(openSession));
                            }
                        }
                    }
                    catch (DateTimeParseException exc) {
                        log.error("Unparseable timestamp in input message", exc);
                    }
                }
            };
        }
    }
}
