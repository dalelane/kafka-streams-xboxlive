package uk.co.dalelane.kafkastreams.xboxlive.streams.enriching;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.dalelane.kafkastreams.xboxlive.data.presence.EnrichedPresence;
import uk.co.dalelane.kafkastreams.xboxlive.data.presence.Presence;
import uk.co.dalelane.kafkastreams.xboxlive.data.serdes.XboxSerdes;
import uk.co.dalelane.kafkastreams.xboxlive.data.users.XboxUserInfo;
import uk.co.dalelane.kafkastreams.xboxlive.data.users.XboxUsers;


/**
 * Example of using Kafka Streams to enrich a stream of events with
 *  additional data from external sources.
 *
 *  Use case: Adding info about the user to presence events which
 *   only identifies users by a numeric user id
 *
 *  Background:
 *   The Xbox API returns presence information (what a user is doing
 *   or if they are offline) identifying the gamer just with a
 *   numeric userid.
 *
 *   This stream processor looks up the user info (name, gamertag,
 *   profile pic) for the user in each event, and adds this to the
 *   event, creating an enriched version of each event.
 *
 *   This is a good example of using Kafka Streams to enrich a
 *   stream of events that it looks up from external systems of
 *   record.
 *
 *   The presence API can return duplicate events, so this stream
 *   processor also does some filtering to remove duplicates or
 *   out-of-sequence events, to make the output stream as clean as
 *   possible.
 *
 *
 *  TOPICS:
 *   Input:     XBOX.PRESENCE
 *   Output:    XBOX.PRESENCE.ENRICHED
 *
 */
public class PresenceEnricher {

    private static Logger log = LoggerFactory.getLogger(PresenceEnricher.class);

    private static final String INPUT_TOPIC  = "XBOX.PRESENCE";
    private static final String OUTPUT_TOPIC = "XBOX.PRESENCE.ENRICHED";


    /**
     * @param xboxUserInfo - info for all known users - this is looked
     *  up at start-up, and reused for the duration of the processor,
     *  to improve performance
     */
    public static void create(final StreamsBuilder builder, final XboxUsers xboxUserInfo) {
        log.info("Creating PresenceEnricher");

        //
        // a "presence" event describes what a gamer is doing
        //  it can identify a game they are playing, a media app they
        //   are watching, or that they are offline
        //

        // keep the last presence event seen for each user - this will produce
        //  a cleaner output stream by letting us identify duplicate events,
        //   or events that are earlier than the last event seen for a user
        final Map<String, Presence> lastPresenceCache = new HashMap<String, Presence>();

        final Predicate<String, Presence> filterOutOfSequenceEvents = new Predicate<>() {
            @Override
            public boolean test(String key, Presence value) {
                final String userid = value.getUserId();

                if (lastPresenceCache.containsKey(userid) == false) {
                    // first presence event observed for this user
                    lastPresenceCache.put(userid, value);
                    return true;
                }

                Presence lastValue = lastPresenceCache.get(userid);
                if (value.getDate().isBefore(lastValue.getDate())) {
                    // this is an older event than the last one seen
                    //  for this user
                    return false;
                }

                // this is a newer event than the last one seen for
                //  this user - this is okay to emit

                // cache in case we see this again
                lastPresenceCache.put(userid, value);

                return true;
            }
        };

        final Predicate<String, Presence> filterMissingData = new Predicate<>() {
            @Override
            public boolean test(String key, Presence event) {
                // user is reported as online, but no game specified...
                if ("Online".equals(event.getState()) && event.getTitleId() == null)
                {
                    return false;
                }

                return true;
            }
        };

        // to improve performance, the list of user info is inserted into a map,
        //  so the user info can be retrieved by userid
        final Map<String, XboxUserInfo> userInfoCache = getUserInfo(xboxUserInfo);


        // maps the stream of presence events to a stream of enrichedpresence events
        //  where an enrichedpresence event is the same as the input presence but
        //    with additional info added about the gamer
        final KeyValueMapper<String, Presence, KeyValue<String, EnrichedPresence>> enrichPresenceEvents = new KeyValueMapper<>() {
            @Override
            public KeyValue<String, EnrichedPresence> apply(String key, Presence presence) {
                XboxUserInfo userInfo;
                String gamertag = "unknown";
                if (userInfoCache.containsKey(presence.getUserId())) {
                    userInfo = userInfoCache.get(presence.getUserId());
                    gamertag = userInfo.getGamertag();
                }
                else {
                    userInfo = new XboxUserInfo();
                    userInfo.setUserId(presence.getUserId());
                }
                EnrichedPresence enriched = new EnrichedPresence(presence, userInfo);
                return KeyValue.pair(gamertag, enriched);
            }
        };



        builder
            // input the presences from the topic produced to by the Kafka Connect connector
            .stream(INPUT_TOPIC, Consumed.with(XboxSerdes.GAMERTAG_SERDES,
                                               XboxSerdes.PRESENCE_SERDES))
            // filter out the noise to leave a cleaner stream
            .filter(filterOutOfSequenceEvents,
                    Named.as("filter_out_of_sequence_presence_events"))
            .filter(filterMissingData,
                    Named.as("filter_games_with_missing_data"))
            // enrich the presence events with additional info about gamers
            .map(enrichPresenceEvents,
                 Named.as("enrich_presence_events_with_user_info"))
            // output the enriched presence events to the XBOX.PRESENCE.ENRICHED topic
            .to(OUTPUT_TOPIC, Produced.valueSerde(XboxSerdes.ENRICHED_PRESENCE_SERDES));
    }



    /**
     * Create a map to allow user info to be looked up by userid.
     */
    private static Map<String, XboxUserInfo> getUserInfo(final XboxUsers xboxUsers) {
        Map<String, XboxUserInfo> userInfoCache = new HashMap<>();

        for (XboxUserInfo userInfo : xboxUsers.getPeople()) {
            userInfoCache.put(userInfo.getUserId(), userInfo);
        }

        return userInfoCache;
    }
}
