package uk.co.dalelane.kafkastreams.xboxlive.streams.splitting;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.dalelane.kafkastreams.xboxlive.data.games.TitleIdInfo;
import uk.co.dalelane.kafkastreams.xboxlive.data.serdes.XboxSerdes;


/**
 * Example of using Kafka Streams to split a stream of events
 *  into multiple separate streams.
 *
 *  Use case: Create streams for different Xbox activities
 *
 *  Background:
 *   The Xbox API for fetching presence events includes
 *   different types of activities - playing games obviously,
 *   but also watching streaming media (e.g. YouTube, Netflix)
 *   and using other apps.
 *
 *   This stream processor splits the single presence stream
 *   into a separate stream for each type of activity.
 *
 *   This is a good example of using Kafka Streams to split a
 *   stream of events with multiple unrelated events into
 *   separate streams dedicated for specific use cases.
 *
 *
 *  TOPICS:
 *   Input:     XBOX.PRESENCE.ENRICHED
 *   Output:    XBOX.PRESENCE.GAME
 *              XBOX.PRESENCE.MEDIA
 *              XBOX.PRESENCE.APP
 *
 */
public class PresenceSplitter {

    private static Logger log = LoggerFactory.getLogger(PresenceSplitter.class);

    private static final String INPUT_TOPIC        = "XBOX.PRESENCE.ENRICHED";

    private static final String OUTPUT_TOPIC_GAME  = "XBOX.PRESENCE." + TitleIdInfo.GAME;
    private static final String OUTPUT_TOPIC_APP   = "XBOX.PRESENCE." + TitleIdInfo.APP;
    private static final String OUTPUT_TOPIC_MEDIA = "XBOX.PRESENCE." + TitleIdInfo.MEDIA_PLAYER;



    public static void create(final StreamsBuilder builder) {
        log.info("Creating PresenceSplitter");



        builder
            // input the enriched presence events - supplemented with additional
            //                  information about the user
            .stream(INPUT_TOPIC, Consumed.with(XboxSerdes.GAMERTAG_SERDES,
                                               XboxSerdes.ENRICHED_PRESENCE_SERDES))
            // filter out events recording when a user goes offline, as they
            //    wont fit into any of the output topics
            .filterNot((gamertag, presenceEvent) -> presenceEvent.userOffline(),
                       Named.as("ignore_offline_events"))
            // split the stream into three branches - one for each type of xbox title
            .split()
                // games
                .branch((key, presence) -> TitleIdInfo.GAME.equals(TitleIdInfo.getTitleType(presence.getTitleId())),
                        Branched.withConsumer(str -> str.to(OUTPUT_TOPIC_GAME)))
                // media
                .branch((key, presence) -> TitleIdInfo.MEDIA_PLAYER.equals(TitleIdInfo.getTitleType(presence.getTitleId())),
                        Branched.withConsumer(str -> str.to(OUTPUT_TOPIC_MEDIA)))
                // apps
                .branch((key, presence) -> TitleIdInfo.APP.equals(TitleIdInfo.getTitleType(presence.getTitleId())),
                        Branched.withConsumer(str -> str.to(OUTPUT_TOPIC_APP)));
    }
}
