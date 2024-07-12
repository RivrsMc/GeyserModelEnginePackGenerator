package io.rivrs.geysermeggenerator.model;

import java.util.List;
import java.util.UUID;

import org.intellij.lang.annotations.Language;

public class PackManifest {

    @Language("JSON")
    public static final String TEMPLATE = """
            {
              "format_version": 2,
              "metadata": {
                "authors": [
                   "Rivrs",
                   "Roch Blondiaux"
                ]
              },
              "header": {
                "name": "%name%",
                "description": "%description%",
                "min_engine_version": [
                    1,
                    20,
                    80
                ],
                "uuid": "%uuid-1%",
                "version": [%version%]
              },
              "modules": [
                {
                  "type": "resources",
                  "description": "ModelEngine",
                  "uuid": "%uuid-2%",
                  "version": [%version%]
                }
              ]
            }
            """;

    public static String generate(String name, String description, List<Long> version) {
        UUID uuid = UUID.nameUUIDFromBytes(name.getBytes());

        return TEMPLATE.replace("%name%", name)
                .replace("%description%", description)
                .replace("%version%", version.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("0"))
                .replace("%uuid-1%", uuid.toString())
                .replace("%uuid-2%", UUID.randomUUID().toString());
    }
}
