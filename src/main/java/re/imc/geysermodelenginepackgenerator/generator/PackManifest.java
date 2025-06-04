package re.imc.geysermodelenginepackgenerator.generator;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import re.imc.geysermodelenginepackgenerator.ExtensionMain;
import re.imc.geysermodelenginepackgenerator.configuration.Configuration;

public class PackManifest {

    public static final String TEMPLATE = """
            {
              "format_version": 1,
              "header": {
                "name": "%name%",
                "description": "%description%",
                "uuid": "%uuid-1%",
                "version": [%version%]
              },
              "modules": [
                {
                  "type": "resources",
                  "description": "%description%",
                  "uuid": "%uuid-2%",
                  "version": [%version%],
                }
              ]
            }
            """;

    public static String generate() {
        Configuration.Pack configuration = ExtensionMain.get().getConfiguration().getPack();


        return TEMPLATE.replace("%uuid-1%", UUID.nameUUIDFromBytes(configuration.name().getBytes(StandardCharsets.UTF_8)).toString())
                .replace("%uuid-2%", UUID.randomUUID().toString())
                .replace("%name%", configuration.name())
                .replace("%description%", configuration.description())
                .replace("%version%", String.join(", ", configuration.version().stream()
                        .map(String::valueOf)
                        .toList()));
    }
}
