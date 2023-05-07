package uk.co.dalelane.kafkastreams.xboxlive.streams.summing;

import java.time.Duration;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.dalelane.kafkastreams.xboxlive.data.achievements.Achievement;
import uk.co.dalelane.kafkastreams.xboxlive.data.achievements.WeeklyGamerScore;
import uk.co.dalelane.kafkastreams.xboxlive.data.serdes.XboxSerdes;


/**
 * Example of using Kafka Streams to maintain a sum of values from
 *  events emitted within a time window.
 *
 *  Use case: Calculating the total gamerscore for each gamer in
 *   the last seven days
 *
 *  Background:
 *   Achievements earned by gamers each include a score, based on
 *   how hard the achievement is to earn and how rare it is.
 *
 *   This stream processor maintains a seven-day window and emits
 *   the total gamerscore of all achievements earned by each
 *   gamer in the last week. This output stream of events represents
 *   a league table for the most successful gamers in the last
 *   week.
 *
 *   This is a good example of using Kafka Streams to maintain
 *   aggregations over a time window.
 *
 *
 *  TOPICS:
 *   Input:     XBOX.ACHIEVEMENTS.KNOWNUSERS
 *   Output:    XBOX.GAMERSCORE.WEEK
 *
 */
public class WeeklyGamerScoreCounter {

    private static Logger log = LoggerFactory.getLogger(WeeklyGamerScoreCounter.class);

    private static final String INPUT_TOPIC  = "XBOX.ACHIEVEMENTS.KNOWNUSERS";
    private static final String OUTPUT_TOPIC = "XBOX.GAMERSCORE.WEEK";

    /** time window for the counter */
    private static final TimeWindows ONE_WEEK = TimeWindows.ofSizeWithNoGrace(Duration.ofDays(7));


    public static void create(final StreamsBuilder builder) {
        log.info("Creating WeeklyGamerScoreCounter");

        /** emits the score for a single achievement event */
        final KeyValueMapper<String, Achievement, KeyValue<String, Integer>> getGamerscoreKeyedByGamertag = new KeyValueMapper<>() {
            @Override
            public KeyValue<String, Integer> apply(String key, Achievement value) {
                return new KeyValue<String, Integer>(value.getGamertag(), value.getGamerScore());
            }
        };

        /** prepare a JSON representation of the output */
        final KeyValueMapper<Windowed<String>, Integer, KeyValue<String, WeeklyGamerScore>> prepareOutput = new KeyValueMapper<>() {
            @Override
            public KeyValue<String, WeeklyGamerScore> apply(Windowed<String> window, Integer value) {
                WeeklyGamerScore output = new WeeklyGamerScore(window.key(), value);
                return new KeyValue<String, WeeklyGamerScore>(window.key(), output);
            }
        };


        builder
            // input the filtered stream of achievement events (filtered to
            //  only include achievements from known users)
            .stream(INPUT_TOPIC, Consumed.with(XboxSerdes.GAMERTAG_SERDES,
                                               XboxSerdes.ACHIEVEMENT_SERDES))
            // emit the score for each achievement event
            .map(getGamerscoreKeyedByGamertag,
                 Named.as("get_gamerscore_keyed_by_gamertag"))
            // group the scores based on the gamer who earned the achievement
            .groupByKey(Grouped.with(XboxSerdes.GAMERTAG_SERDES, Serdes.Integer()))
            // window over a 7-day period
            .windowedBy(ONE_WEEK)
            // sum the scores within the window
            .reduce(Integer::sum, Named.as("sum_weekly_gamerscore"))
            // prepare the output
            .toStream(Named.as("output_weekly_score_stream"))
            .map(prepareOutput,
                 Named.as("convert_to_json_output"))
            // output to the XBOX.GAMERSCORE.WEEK topic with an event for each gamer
            .to(OUTPUT_TOPIC, Produced.with(XboxSerdes.GAMERTAG_SERDES,
                                            XboxSerdes.WEEKLY_SCORE_SERDES));
    }
}
