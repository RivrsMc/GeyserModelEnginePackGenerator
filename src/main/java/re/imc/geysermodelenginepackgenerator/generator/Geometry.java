package re.imc.geysermodelenginepackgenerator.generator;

import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Geometry {

    private String modelId;
    private String geometryId;
    private JsonObject json;
    private Map<String, Bone> bones = new HashMap<>();

    String path;
    public void load(String json) {
        this.json = JsonParser.parseString(json).getAsJsonObject();
    }
    public void setId(String id) {
        geometryId = id;
        getInternal().get("description").getAsJsonObject().addProperty("identifier", id);
    }

    public void setTextureWidth(int w) {
        getInternal().get("description").getAsJsonObject().addProperty("texture_width", w);
    }

    public void setTextureHeight(int h) {
        getInternal().get("description").getAsJsonObject().addProperty("texture_height", h);
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

                String parent = element.getAsJsonObject().has("parent") ? element.getAsJsonObject().get("parent").getAsString().toLowerCase() : null;
                element.getAsJsonObject().remove("name");

                element.getAsJsonObject().addProperty("name", name);

                if (name.equals("hitbox") ||
                        name.equals("shadow") ||
                        name.equals("mount") ||
                        name.startsWith("b_") ||
                        name.startsWith("ob_")) {
                    iterator.remove();
                } else bones.put(name, new Bone(name, parent, new HashSet<>(), new HashSet<>()));
            }

            for (Bone bone : bones.values()) {
                if (bone.getParent() != null) {
                    Bone parent = bones.get(bone.getParent());
                    if (parent != null) {
                        parent.getChildren().add(bone);
                        addAllChildren(parent, bone);
                    }
                }
            }
        }
        setId("geometry.meg_" + modelId);
    }

    public void addAllChildren(Bone p, Bone c) {
        p.getAllChildren().add(c);
        Bone parent = bones.get(p.getParent());
        if (parent != null) {
            addAllChildren(parent, c);
        }
    }
}
