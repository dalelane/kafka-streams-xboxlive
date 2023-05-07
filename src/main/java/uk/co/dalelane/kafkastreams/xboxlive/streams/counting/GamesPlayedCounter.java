package uk.co.dalelane.kafkastreams.xboxlive.streams.counting;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.dalelane.kafkastreams.xboxlive.data.counts.PlayerGame;
import uk.co.dalelane.kafkastreams.xboxlive.data.counts.PlayerGames;
import uk.co.dalelane.kafkastreams.xboxlive.data.presence.EnrichedPresence;
import uk.co.dalelane.kafkastreams.xboxlive.data.serdes.XboxSerdes;

/**
 * Example of using Kafka Streams to maintain a count of times that
 *  things have been seen in a stream of events.
 *
 *  Use case: Counting how many different games each user has played
 *
 *  Background:
 *   The Xbox API returns events when a user plays a game. By tracking
 *   these events, we can maintain a count of how many unique games
 *   each player has tried.
 *
 *   This stream processor uses the filtered stream of game events
 *   created by PresenceSplitter so that uses of media players or
 *   other apps don't contribute to the count.
 *
 *   This is a good example of using Kafka Streams to aggregate
 *   over a stream of events, such as maintaining the min/max/avg/sum
 *   of a value in a stream.
 *
 *
 *  TOPICS:
 *   Input:     XBOX.PRESENCE.GAME
 *   Output:    XBOX.GAMES.COUNTS
 *
 */
public class GamesPlayedCounter {

    private static Logger log = LoggerFactory.getLogger(GamesPlayedCounter.class);

    private static final String INPUT_TOPIC  = "XBOX.PRESENCE.GAME";
    private static final String OUTPUT_TOPIC = "XBOX.GAMES.COUNTS";


    public static void create(final StreamsBuilder builder) {
        log.info("Creating GamesPlayedCounter");

        // we'll output the count as a raw integer - this means
        //  consumers will need to consume it with an integer deserializer
        //  to read these events
        final Serde<Integer> countsSerde = Serdes.Integer();

        //
        // initialise the aggregation state
        final Initializer<PlayerGames> initCount = new Initializer<>() {
            @Override
            public PlayerGames apply() {
                return new PlayerGames();
            }
        };

        //
        // update the state with a new presence event
        final Aggregator<String, EnrichedPresence, PlayerGames> updateCount = new Aggregator<>() {
            @Override
            public PlayerGames apply(String key, EnrichedPresence value, PlayerGames aggregate) {
                aggregate.setGamertag(value.getGamertag());
                aggregate.setRealname(value.getRealName());
                if (aggregate.getGamesIndexedById().containsKey(value.getTitleId()) == false) {
                    PlayerGame game = new PlayerGame(value.getTitleId(), value.getTitleName());
                    aggregate.getGamesIndexedById().put(value.getTitleId(), game);
                }
                return aggregate;
            }
        };

        //
        // output the count based on the aggregation state
        final KeyValueMapper<String, PlayerGames, KeyValue<String, Integer>> outputCount = new KeyValueMapper<> () {
            @Override
            public KeyValue<String, Integer> apply(String key, PlayerGames value) {
                return KeyValue.pair(key, value.getGamesIndexedById().size());
            }
        };


        builder
            // input the filtered stream of game-only events
            .stream(INPUT_TOPIC, Consumed.with(XboxSerdes.GAMERTAG_SERDES,
                                               XboxSerdes.ENRICHED_PRESENCE_SERDES))
            // filter out events without a known game id
            .filterNot((gamertag, presenceEvent) -> presenceEvent.getTitleId() == null,
                       Named.as("ignore_missing_game_titles"))
            // group the events by the gamer to allow the state to be kept
            //  safe for each individual gamer
            .groupByKey()
            // build and maintain an aggregation - storing the list of game titles
            //  for each gamer
            .aggregate(initCount, updateCount,
                       Named.as("track_distinct_games_by_gamertag"),
                       Materialized.with(XboxSerdes.GAMERTAG_SERDES,
                                         XboxSerdes.GAME_COUNTS_SERDES))
            // prepare an output stream that counts the number of distinct titles
            //  for each gamer
            .toStream()
            .map(outputCount,
                 Named.as("prepare_counts_output"))
            // output the number of games played by each user
            .to(OUTPUT_TOPIC, Produced.with(XboxSerdes.GAMERTAG_SERDES,
                                            countsSerde));
    }
}
