package com.example.demo.lease.Controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.Map;

@JsonComponent
public class NullKeySerializer extends JsonSerializer<Map<?, ?>> {

    @Override
    public void serialize(Map<?, ?> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                jsonGenerator.writeFieldName(String.valueOf(entry.getKey()));
                serializerProvider.defaultSerializeValue(entry.getValue(), jsonGenerator);
            }
        }
        jsonGenerator.writeEndObject();
    }
}
