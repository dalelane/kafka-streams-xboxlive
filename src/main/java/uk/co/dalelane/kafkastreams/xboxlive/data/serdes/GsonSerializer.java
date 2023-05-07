package uk.co.dalelane.kafkastreams.xboxlive.data.serdes;

import org.apache.kafka.common.serialization.Serializer;

import java.time.Duration;
import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonSerializer<T> implements Serializer<T> {

    private Class<T> target;
    private Gson jsonParser;

    public GsonSerializer(Class<T> targetClass) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Instant.class, new InstantSerializer());
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationSerializer());

        jsonParser = gsonBuilder.create();
        target = targetClass;
    }

    @Override
    public byte[] serialize(String topic, T data) {
        return jsonParser.toJson(data, target).getBytes();
    }
}
