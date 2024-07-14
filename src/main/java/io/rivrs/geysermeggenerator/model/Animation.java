package io.rivrs.geysermeggenerator.model;

import java.nio.file.Path;
import java.util.Map;

import org.intellij.lang.annotations.Language;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Animation {

    @Language("JSON")
    public static final String HEAD_TEMPLATE = """
             {
               "relative_to" : {
                 "rotation" : "entity"
               },
               "rotation" : [ "query.target_x_rotation - this", "query.target_y_rotation - this", 0.0 ]
            }
            """;

    private final String modelId;
    private final Path path;
    private final JsonObject json;

    public Animation(String modelId, Path path, String json) {
        this.modelId = modelId;
        this.path = path;
        this.json = JsonParser.parseString(json).getAsJsonObject();
    }

    public void modify(Entity entity) {
        JsonObject newAnimations = new JsonObject();
        for (Map.Entry<String, JsonElement> element : json.get("animations").getAsJsonObject().entrySet()) {
            if (element.getKey().equals("spawn"))
                entity.setHasSpawnAnimation(true);
            if (element.getKey().equals("walk"))
                entity.setHasWalkAnimation(true);

            // Remove timeline from animations
            // TODO: Add support for timeline
            if (element.getValue().getAsJsonObject().has("timeline"))
                element.getValue().getAsJsonObject().remove("timeline");

            newAnimations.add("animation." + modelId + "." + element.getKey(), element.getValue());
        }
        json.add("animations", newAnimations);
    }

    public void addHeadBind(Entity entity, Geometry geometry) {
        JsonObject object = new JsonObject();
        object.addProperty("loop", true);
        JsonObject bones = new JsonObject();
        JsonArray array = geometry.getInternal().get("bones").getAsJsonArray();
        int i = 0;
        for (JsonElement element : array) {
            if (element.isJsonObject()) {
                String name = element.getAsJsonObject().get("name").getAsString();

                String parent = "";
                if (element.getAsJsonObject().has("parent")) {
                    parent = element.getAsJsonObject().get("parent").getAsString();
                }
                if (parent.startsWith("h_") || parent.startsWith("hi_")) {
                    continue;
                }
                if (name.startsWith("h_") || name.startsWith("hi_")) {
                    bones.add(name, JsonParser.parseString(HEAD_TEMPLATE));
                    i++;
                }
            }
        }
        if (i == 0) {
            return;
        }
        entity.setHasHeadAnimation(true);

        object.add("bones", bones);
        json.get("animations").getAsJsonObject().add("animation." + modelId + ".look_at_target", object);
    }
}
