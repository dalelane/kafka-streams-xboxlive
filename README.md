# kafka-streams-xboxlive

kafka-streams-xboxlive is a Kafka Streams demonstration using Xbox LIVE data from [kafka-connect-xboxlive-source](https://github.com/dalelane/kafka-connect-xboxlive-source).

The aim of the project is to demonstrate what Kafka Streams can do, using data from Xbox LIVE.

## Examples

See comments at the top of each example for a more detailed explanation:

- **FILTERING out unwanted events**
    - use case: removing achievements from unknown users
    - see [`AchievementsKnownUsersFilter`](https://github.com/dalelane/kafka-streams-xboxlive/blob/master/src/main/java/uk/co/dalelane/kafkastreams/xboxlive/streams/filtering/AchievementsKnownUsersFilter.java#L22)
- **SPLITTING out a stream into multiple separate streams**
    - use case: create streams for different Xbox activities
    - see [`PresenceSplitter`](https://github.com/dalelane/kafka-streams-xboxlive/blob/master/src/main/java/uk/co/dalelane/kafkastreams/xboxlive/streams/splitting/PresenceSplitter.java#L15)
- **ENRICHING events with additional info from other sources**
    - use case: adding info about users to presence events
    - see [`PresenceEnricher`](https://github.com/dalelane/kafka-streams-xboxlive/blob/master/src/main/java/uk/co/dalelane/kafkastreams/xboxlive/streams/enriching/PresenceEnricher.java#L24)
- **MATCHING related events to identify complex events**
    - use case: match related gaming start and stop events
    - see [`PlaySessionGenerator`](https://github.com/dalelane/kafka-streams-xboxlive/blob/master/src/main/java/uk/co/dalelane/kafkastreams/xboxlive/streams/matching/PlaySessionGenerator.java#L27)
- **COUNTING how many times things happen in events**
    - use case: counting how many games each user has played
    - see [`GamesPlayedCounter`](https://github.com/dalelane/kafka-streams-xboxlive/blob/master/src/main/java/uk/co/dalelane/kafkastreams/xboxlive/streams/counting/GamesPlayedCounter.java#L23)
- **SUMMING values from events in a time window**
    - use case: calculate total game score over 7-day window
    - see [`WeeklyGamerScoreCounter`](https://github.com/dalelane/kafka-streams-xboxlive/blob/master/src/main/java/uk/co/dalelane/kafkastreams/xboxlive/streams/summing/WeeklyGamerScoreCounter.java#L24)
- **TRACKING the most significant events seen so far**
    - use case: tracking the longest play sessions seen so far
    - see [`LongestPlayRecordTracker`](https://github.com/dalelane/kafka-streams-xboxlive/blob/master/src/main/java/uk/co/dalelane/kafkastreams/xboxlive/streams/tracking/LongestPlayRecordTracker.java#L18)


## Building

```sh
mvn package
```

## Acknowledgements

This project is not in any way official or affiliated with Microsoft or Xbox. It uses data from Xbox fetched using [OpenXBL](https://xbl.io/) - which is an unofficial API for getting data from Xbox LIVE.