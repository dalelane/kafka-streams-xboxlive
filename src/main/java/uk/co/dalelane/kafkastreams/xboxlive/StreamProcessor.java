package uk.co.dalelane.kafkastreams.xboxlive;

import java.util.Properties;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.dalelane.kafkastreams.xboxlive.data.users.XboxUsers;
import uk.co.dalelane.kafkastreams.xboxlive.data.users.XboxUsersFactory;
import uk.co.dalelane.kafkastreams.xboxlive.streams.counting.GamesPlayedCounter;
import uk.co.dalelane.kafkastreams.xboxlive.streams.enriching.PresenceEnricher;
import uk.co.dalelane.kafkastreams.xboxlive.streams.filtering.AchievementsKnownUsersFilter;
import uk.co.dalelane.kafkastreams.xboxlive.streams.matching.PlaySessionGenerator;
import uk.co.dalelane.kafkastreams.xboxlive.streams.splitting.PresenceSplitter;
import uk.co.dalelane.kafkastreams.xboxlive.streams.summing.WeeklyGamerScoreCounter;
import uk.co.dalelane.kafkastreams.xboxlive.streams.tracking.LongestPlayRecordTracker;
import uk.co.dalelane.kafkastreams.xboxlive.utils.Config;

public class StreamProcessor {

    private static Logger log = LoggerFactory.getLogger(StreamProcessor.class);


    public static void main(String[] args) {
        log.info("Xbox Live Kafka Streams demo");

        final Properties props = Config.getStreamsConfiguration();
        final XboxUsers xboxUsers = XboxUsersFactory.getXboxUsers(props.getProperty(Config.XBOX_API_KEY));

        final StreamsBuilder builder = new StreamsBuilder();


        // ==========================================================
        // FILTERING out unwanted events
        // ==========================================================
        //  use case:  removing achievements from unknown users
        // ----------------------------------------------------------
        //   XBOX.ACHIEVEMENTS     -->   XBOX.ACHIEVEMENTS.KNOWNUSERS
        // ----------------------------------------------------------
        AchievementsKnownUsersFilter.create(builder, xboxUsers);


        // ==========================================================
        // SPLITTING out a stream into multiple separate streams
        // ==========================================================
        //  use case:  create streams for different Xbox activities
        // ----------------------------------------------------------
        //   XBOX.PRESENCE.ENRICHED      -->   XBOX.PRESENCE.GAME
        //                                     XBOX.PRESENCE.MEDIA
        //                                     XBOX.PRESENCE.APP
        // ----------------------------------------------------------
        PresenceSplitter.create(builder);


        // ==========================================================
        // ENRICHING events with additional info from other sources
        // ==========================================================
        //  use case:  adding info about users to presence events
        // ----------------------------------------------------------
        //   XBOX.PRESENCE               -->   XBOX.PRESENCE.ENRICHED
        // ----------------------------------------------------------
        PresenceEnricher.create(builder, xboxUsers);


        // ==========================================================
        // MATCHING related events to identify complex events
        // ==========================================================
        //  use case:  match related gaming start and stop events
        // ----------------------------------------------------------
        //   XBOX.PRESENCE.ENRICHED      -->   XBOX.PLAYSESSIONS
        // ----------------------------------------------------------
        PlaySessionGenerator.create(builder);


        // ==========================================================
        // COUNTING how many times things happen in events
        // ==========================================================
        //  use case:  counting how many games each user has played
        // ----------------------------------------------------------
        //     XBOX.PRESENCE.GAME        -->   XBOX.GAMES.COUNTS
        // ----------------------------------------------------------
        GamesPlayedCounter.create(builder);


        // ==========================================================
        // SUMMING values from events in a time window
        // ==========================================================
        //  use case:  calculate total game score over 7-day window
        // ----------------------------------------------------------
        //   XBOX.ACHIEVEMENTS.KNOWNUSERS  -->   XBOX.GAMERSCORE.WEEK
        // ----------------------------------------------------------
        WeeklyGamerScoreCounter.create(builder);


        // ==========================================================
        // TRACKING the most significant events seen so far
        // ==========================================================
        //  use case:  tracking the longest play sessions seen so far
        // ----------------------------------------------------------
        //   XBOX.PLAYSESSIONS        -->   XBOX.PLAYSESSIONS.LONGEST
        // ----------------------------------------------------------
        LongestPlayRecordTracker.create(builder);





        final Topology topology = builder.build();
        log.info(topology.describe().toString());
        final KafkaStreams streams = new KafkaStreams(topology, props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
