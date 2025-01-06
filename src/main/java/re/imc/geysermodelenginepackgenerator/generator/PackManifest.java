package re.imc.geysermodelenginepackgenerator.generator;

import java.util.UUID;
import re.imc.geysermodelenginepackgenerator.ExtensionMain;

public class PackManifest {
    public static final String TEMPLATE = """
            {
              "format_version": 1,
              "header": {
                "name": %name%,
                "description": %description%,
                "uuid": "%uuid-1%",
                "version": %version%
              },
              "modules": [
                {
                  "type": "resources",
                  "description": %description%,
                  "uuid": "%uuid-2%",
                  "version": %version%
                }
              ]
            }
            """;

    public static String generate(ExtensionMain main) {
        String name = main.getConfiguration().getPackName();
        UUID uniqueId = UUID.nameUUIDFromBytes(name.getBytes());

        String formattedVersion = "1, 0, 0";
        String version = main.getConfiguration().getPackVersion();
        if (version.contains(".")) {
            String[] split = version.split("\\.");
            if (split.length == 1) {
                formattedVersion = split[0] + ", 0, 0";
            } else if (split.length == 2) {
                formattedVersion = split[0] + ", " + split[1] + ", 0";
            } else if (split.length == 3) {
                formattedVersion = split[0] + ", " + split[1] + ", " + split[2];
            }
        }

        return TEMPLATE.replace("%uuid-1%", uniqueId.toString())
                .replace("%uuid-2%", UUID.randomUUID().toString())
                .replace("%name%", name)
                .replace("%description%", main.getConfiguration().getPackDescription())
                .replace("%version%", "[%s]".formatted(formattedVersion));
    }
}
