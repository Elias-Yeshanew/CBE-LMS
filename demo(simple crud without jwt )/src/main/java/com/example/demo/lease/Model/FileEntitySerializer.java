package com.example.demo.lease.Model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class FileEntitySerializer extends JsonSerializer<FileEntity> {

    @Override
    public void serialize(FileEntity fileEntity, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        if (fileEntity != null) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("fileName", fileEntity.getFileName());
            jsonGenerator.writeStringField("fileType", fileEntity.getFileType());
            jsonGenerator.writeStringField("filePath", fileEntity.getFilePath());
            // Add other fields as needed
            jsonGenerator.writeEndObject();
        } else {
            jsonGenerator.writeNull();
        }
    }
}
