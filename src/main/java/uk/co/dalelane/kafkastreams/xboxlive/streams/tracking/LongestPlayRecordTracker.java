package uk.co.dalelane.kafkastreams.xboxlive.streams.tracking;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.dalelane.kafkastreams.xboxlive.data.serdes.XboxSerdes;
import uk.co.dalelane.kafkastreams.xboxlive.data.sessions.PlaySession;


/**
 * Example of using Kafka Streams to maintain the most signifcant
 *  event in a stream of events.
 *
 *  Use case: Maintaining the longest play session for each gamer.
 *
 *  Background:
 *   The presence topic records events when users start and stop
 *   playing individual games.
 *
 *   These events are enriched with additional information about
 *   the gamer using a different Xbox API by the PresenceEnricher
 *   stream processor.
 *
 *   These enriched-but-still-raw-and-low-level events are then
 *   paired into "play sessions" representing a completed session
 *   with a start and stop time by the PlaySessionGenerator stream
 *   processor.
 *
 *   This stream processor takes those play session events and
 *   tracks the longest play session for each gamer.
 *
 *  TOPICS:
 *   Input:     XBOX.PLAYSESSIONS
 *   Output:    XBOX.PLAYSESSIONS.LONGEST
 *
 */
public class LongestPlayRecordTracker {

    private static Logger log = LoggerFactory.getLogger(LongestPlayRecordTracker.class);

    private static final String INPUT_TOPIC  = "XBOX.PLAYSESSIONS";
    private static final String OUTPUT_TOPIC = "XBOX.PLAYSESSIONS.LONGEST";


    public static void create(final StreamsBuilder builder) {
        log.info("Creating LongestPlayRecordTracker");

        final Initializer<PlaySession> initSession = new Initializer<>() {
            @Override
            public PlaySession apply() {
                return null;
            }
        };

        final Aggregator<String, PlaySession, PlaySession> longestDuration = new Aggregator<>() {
            @Override
            public PlaySession apply(String key, PlaySession newSession, PlaySession longestSoFar) {
                if (newSession == null && longestSoFar == null) {
                    return null;
                }
                if (longestSoFar == null) {
                    return newSession;
                }

                if (newSession.getDuration().compareTo(longestSoFar.getDuration()) > 0) {
                    return newSession;
                }
                else {
                    return longestSoFar;
                }
            }
        };

        builder
            // input the stream of play sessions - completed logical events with
            //         a start and stop time for playing a single game
            .stream(INPUT_TOPIC, Consumed.with(XboxSerdes.GAMERTAG_SERDES,
                                               XboxSerdes.PLAY_SESSION_SERDES))
            // group by the gamer, so that events for any given gamer can
            //  be processed together
            .groupByKey()
            // get the longest play session for each gamer
            .aggregate(initSession, longestDuration,
                       Named.as("get_longest_play_session"),
                       Materialized.with(XboxSerdes.GAMERTAG_SERDES,
                                         XboxSerdes.PLAY_SESSION_SERDES))
            // output the longest play session for each gamer to the
            //  XBOX.PLAYSESSIONS.LONGEST topic
            .toStream()
            .to(OUTPUT_TOPIC, Produced.with(XboxSerdes.GAMERTAG_SERDES,
                                            XboxSerdes.PLAY_SESSION_SERDES));
    }
}
