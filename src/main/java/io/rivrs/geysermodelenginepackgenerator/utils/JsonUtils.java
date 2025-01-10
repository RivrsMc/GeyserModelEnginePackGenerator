package io.rivrs.geysermodelenginepackgenerator.utils;

import com.google.gson.JsonElement;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtils {

    public static boolean isGeometry(JsonElement element) {
        return element.isJsonObject() && element.getAsJsonObject().has("minecraft:geometry");
    }

    public static boolean isAnimation(JsonElement element) {
        return element.isJsonObject() && element.getAsJsonObject().has("animations");
    }
}
