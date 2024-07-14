package io.rivrs.geysermeggenerator.configuration.adapter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class PathTypeAdapter extends TypeAdapter<Path> {

    @Override
    public void write(JsonWriter jsonWriter, Path path) throws IOException {
        jsonWriter.value(path.toString());
    }

    @Override
    public Path read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == null)
            return null;

        return Paths.get(jsonReader.nextString());
    }
}
