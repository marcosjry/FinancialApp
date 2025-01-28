package com.marcos.budget.service.domain.config.infra;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeAdapter implements JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;  // Evita serializar null como uma string
        }

        try {
            return new JsonPrimitive(src.format(formatter));  // Formatação para o formato ISO
        } catch (Exception e) {
            throw new JsonParseException("Error during LocalDateTime serialization", e);
        }
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;  // Evita erro se o valor for nulo
        }

        try {
            return LocalDateTime.parse(json.getAsString(), formatter);  // Deserialização de string para LocalDateTime
        } catch (DateTimeParseException e) {
            throw new JsonParseException("Invalid date format for LocalDateTime", e);
        }
    }
}
