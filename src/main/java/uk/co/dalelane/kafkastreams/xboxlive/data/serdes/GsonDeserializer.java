package uk.co.dalelane.kafkastreams.xboxlive.data.serdes;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.kafka.common.serialization.Deserializer;

public class GsonDeserializer<T> implements Deserializer<T> {

    private Class<T> target;
    private Gson jsonParser;

    public GsonDeserializer(Class<T> targetClass) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Instant.class, new InstantDeserializer());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationDeserializer());

        jsonParser = gsonBuilder.create();
        target = targetClass;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        String stringData = new String(data, StandardCharsets.UTF_8);
        return jsonParser.fromJson(stringData, target);
    }
}
