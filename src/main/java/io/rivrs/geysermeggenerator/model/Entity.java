package io.rivrs.geysermeggenerator.model;

import java.nio.file.Path;
import java.util.Properties;

import org.intellij.lang.annotations.Language;

import lombok.Data;

@Data
public class Entity {

    @Language("JSON")
    public static final String TEMPLATE = """
            {
              "format_version": "1.10.0",
              "minecraft:client_entity": {
                "description": {
                  "identifier": "modelengine:%entity_id%",
                  "materials": {
                    "default": "%material%"
                  },
                  "textures": {
                    "default": "%texture%"
                  },
                  "geometry": {
                    "default": "%geometry%"
                  },
                  "animations": {
                    "idle": "animation.%entity_id%.idle",
                    "spawn": "animation.%entity_id%.%spawn%",
                    "walk": "animation.%entity_id%.%walk%",
                    "look_at_target": "%look_at_target%",
                    "modelengine_controller": "controller.animation.modelengine"
                  },
                  "scripts": {
                    "animate": [
                      "modelengine_controller",
                      "look_at_target"
                    ]
                  },
                  "render_controllers": [
                    "%render_controller%"
                  ]
                }
              }
            }
            """;

    private final String modelId;
    private final Path path;
    private String json;
    private boolean hasHeadAnimation;
    private boolean hasWalkAnimation;
    private boolean hasSpawnAnimation;

    private final Properties properties = new Properties();

    public void modify(String path) {
        String walk;
        String spawn;
        walk = spawn = "idle";
        if (hasWalkAnimation) {
            walk = "walk";
        }
        if (hasSpawnAnimation) {
            spawn = "spawn";
        }
        json = TEMPLATE.replace("%entity_id%", modelId)
                .replace("%geometry%", "geometry.modelengine_" + modelId)
                .replace("%texture%", "textures/entity/" + path + "/" + modelId)
                .replace("%look_at_target%", "animation." + modelId + ".look_at_target")
                .replace("%walk%", walk)
                .replace("%spawn%", spawn)
                .replace("%material%", properties.getProperty("material", "entity_alphatest_change_color"))
                .replace("%render_controller%", properties.getProperty("render_controller", "controller.render.default"));
    }

}
