package uk.co.dalelane.kafkastreams.xboxlive.streams.filtering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.dalelane.kafkastreams.xboxlive.data.achievements.Achievement;
import uk.co.dalelane.kafkastreams.xboxlive.data.serdes.XboxSerdes;
import uk.co.dalelane.kafkastreams.xboxlive.data.users.XboxUserInfo;
import uk.co.dalelane.kafkastreams.xboxlive.data.users.XboxUsers;

/**
 * Example of using Kafka Streams to filter a stream of events.
 *
 *  Use case: Filter out achievements from unknown users
 *
 *  Background:
 *   The Xbox API for fetching achievements from gamers in your
 *   social network sometimes returns achievements from gamers
 *   that you don't follow.
 *
 *   This stream processor creates a filtered stream of the
 *   achievements stream, matching only achievements from gamers
 *   in the known users list.
 *
 *   This is a good example of using Kafka Streams to clean a
 *   noisy stream of events from an external source as part of
 *   preparing it for use.
 *
 *
 *  TOPICS:
 *   Input:     XBOX.ACHIEVEMENTS
 *   Output:    XBOX.ACHIEVEMENTS.KNOWNUSERS
 *
 */
public class AchievementsKnownUsersFilter {

    private static Logger log = LoggerFactory.getLogger(AchievementsKnownUsersFilter.class);

    private static final String INPUT_TOPIC  = "XBOX.ACHIEVEMENTS";
    private static final String OUTPUT_TOPIC = "XBOX.ACHIEVEMENTS.KNOWNUSERS";



    /**
     * @param knownUsers - list of users in your gaming social network. Only
     *   achievements from one of these users will emitted.
     */
    public static void create(final StreamsBuilder builder, final XboxUsers knownUsers) {
        log.info("Creating AchievementsKnownUsersFilter");

        // get the gamertags from the provided list
        //  get a sorted list so that the filtering at runtime can be a little bit
        //  more performant
        final String[] knownUserGamertags = getGamerTags(knownUsers);

        // predicate that returns true for achievements from one of the known gamers
        final Predicate<String, Achievement> knownUsersFilter = new Predicate<>() {
            @Override
            public boolean test(String key, Achievement value) {
                return Arrays.binarySearch(knownUserGamertags, value.getGamertag()) >= 0;
            }
        };



        builder
            // input the achievements from the topic produced to by the Kafka Connect connector
            .stream(INPUT_TOPIC, Consumed.with(XboxSerdes.GAMERTAG_SERDES,
                                               XboxSerdes.ACHIEVEMENT_SERDES))
            // filter out the achievement events from unknown users
            .filter(knownUsersFilter,
                    Named.as("filter_known_users_only"))
            // output the achievement events as-is to the XBOX.ACHIEVEMENTS.KNOWNUSERS topic
            .to(OUTPUT_TOPIC, Produced.with(XboxSerdes.GAMERTAG_SERDES,
                                            XboxSerdes.ACHIEVEMENT_SERDES));
    }




    /**
     * Gets a sorted list of gamertags from the provided list of known users.
     */
    private static String[] getGamerTags(final XboxUsers knownUsers) {
        log.debug("Getting gamertags from the list of known xbox users");

        List<String> gamertags = new ArrayList<>();

        for (XboxUserInfo userInfo : knownUsers.getPeople()) {
            gamertags.add(userInfo.getGamertag());
        }
        Collections.sort(gamertags);

        return gamertags.toArray(new String[0]);
    }
}
