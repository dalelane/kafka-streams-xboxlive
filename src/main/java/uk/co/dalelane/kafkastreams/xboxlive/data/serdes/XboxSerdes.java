package uk.co.dalelane.kafkastreams.xboxlive.data.serdes;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import uk.co.dalelane.kafkastreams.xboxlive.data.achievements.Achievement;
import uk.co.dalelane.kafkastreams.xboxlive.data.achievements.WeeklyGamerScore;
import uk.co.dalelane.kafkastreams.xboxlive.data.counts.PlayerGames;
import uk.co.dalelane.kafkastreams.xboxlive.data.presence.EnrichedPresence;
import uk.co.dalelane.kafkastreams.xboxlive.data.presence.Presence;
import uk.co.dalelane.kafkastreams.xboxlive.data.sessions.PlaySession;

public class XboxSerdes {

    private static <T> Serde<T> createSerdes(Class<T> clazz) {
        GsonSerializer<T> serializer = new GsonSerializer<>(clazz);
        GsonDeserializer<T> deserializer = new GsonDeserializer<>(clazz);
        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static final Serde<String> GAMERTAG_SERDES = Serdes.String();

    public static final Serde<Achievement> ACHIEVEMENT_SERDES = createSerdes(Achievement.class);

    public static final Serde<Presence> PRESENCE_SERDES = createSerdes(Presence.class);

    public static final Serde<EnrichedPresence> ENRICHED_PRESENCE_SERDES = createSerdes(EnrichedPresence.class);

    public static final Serde<PlayerGames> GAME_COUNTS_SERDES = createSerdes(PlayerGames.class);

    public static final Serde<WeeklyGamerScore> WEEKLY_SCORE_SERDES = createSerdes(WeeklyGamerScore.class);

    public static final Serde<PlaySession> PLAY_SESSION_SERDES = createSerdes(PlaySession.class);
}
