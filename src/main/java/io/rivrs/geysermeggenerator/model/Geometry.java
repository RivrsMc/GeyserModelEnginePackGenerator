package io.rivrs.geysermeggenerator.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Geometry {

    private final String modelId;
    private final JsonObject json;
    private final List<String> bones = new ArrayList<>();
    private final Path path;

    public Geometry(String modelId, Path path, String json) {
        this.modelId = modelId;
        this.path = path;
        this.json = JsonParser.parseString(json).getAsJsonObject();
    }

    public void setId(String id) {
        getInternal().get("description").getAsJsonObject().addProperty("identifier", id);
    }

    public JsonObject getInternal() {
        return json.get("minecraft:geometry").getAsJsonArray().get(0)
                .getAsJsonObject();
    }

    public void modify() {
        JsonArray array = getInternal().get("bones").getAsJsonArray();
        Iterator<JsonElement> iterator = array.iterator();
        while (iterator.hasNext()) {
            JsonElement element = iterator.next();
            if (element.isJsonObject()) {
                String name = element.getAsJsonObject().get("name").getAsString().toLowerCase(Locale.ROOT);

                element.getAsJsonObject().remove("name");
                element.getAsJsonObject().addProperty("name", name);

                if (name.equals("hitbox") ||
                    name.equals("mount") ||
                    name.startsWith("p_") ||
                    name.startsWith("b_") ||
                    name.startsWith("ob_")) {
                    iterator.remove();
                } else bones.add(name);
            }
        }
        setId("geometry.modelengine_" + modelId);
    }

}
