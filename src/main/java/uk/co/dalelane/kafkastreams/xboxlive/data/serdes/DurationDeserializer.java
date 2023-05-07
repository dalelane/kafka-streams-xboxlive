package uk.co.dalelane.kafkastreams.xboxlive.data.serdes;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class DurationDeserializer implements JsonDeserializer<Duration> {

    @Override
    public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Duration.of(json.getAsLong(), ChronoUnit.SECONDS);
    }
}
