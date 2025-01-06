package re.imc.geysermodelenginepackgenerator.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import re.imc.geysermodelenginepackgenerator.ExtensionMain;

public class Configuration {

    private final Properties properties = new Properties();
    private final Path path;

    public Configuration(ExtensionMain extension) {
        this.path = extension.dataFolder().resolve("config.properties");
        this.load();
    }

    public void load() {
        if (!Files.isDirectory(path.getParent())) {
            try {
                Files.createDirectories(path.getParent());
            } catch (Exception e) {
                ExtensionMain.logger.error("Failed to create config file", e);
            }
        }

        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
                properties.setProperty("name", "GeyserModelEnginePack");
                properties.setProperty("description", "Generated pack by GeyserModelEnginePackGenerator");
                properties.setProperty("version", "1.0.0");
                properties.setProperty("auto-load", "true");

                try {
                    properties.store(Files.newBufferedWriter(path), "GeyserModelEnginePackGenerator Configuration");
                } catch (Exception e) {
                    ExtensionMain.logger.error("Failed to create config file", e);
                }
            } catch (Exception e) {
                ExtensionMain.logger.error("Failed to create config file", e);
            }
        }

        try {
            properties.load(Files.newBufferedReader(path));
        } catch (Exception e) {
            ExtensionMain.logger.error("Failed to load config file", e);
        }
    }

    public String getPackName() {
        return properties.getProperty("name", "GeyserModelEnginePack");
    }

    public String getPackDescription() {
        return properties.getProperty("description", "Generated pack by GeyserModelEnginePackGenerator");
    }

    public String getPackVersion() {
        return properties.getProperty("version", "1.0.0");
    }

    public boolean isAutoLoad() {
        return Boolean.parseBoolean(properties.getProperty("auto-load", "true"));
    }


}
