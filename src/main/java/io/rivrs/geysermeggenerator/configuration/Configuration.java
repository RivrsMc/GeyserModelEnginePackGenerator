package io.rivrs.geysermeggenerator.configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.moandjiezana.toml.Toml;

import lombok.Data;

@Data
public class Configuration {

    private final Path path;
    private Toml toml;

    public void load() {
        if (!Files.exists(this.path))
            this.loadDefault();

        this.toml = new Toml().read(this.path.toFile());
    }

    private void loadDefault() {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("config.toml")) {
            if (inputStream == null)
                throw new RuntimeException("Default configuration not found.");
            Files.copy(inputStream, this.path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load default configuration.", e);
        }
    }

    // General
    public Toml general() {
        return this.toml.getTable("general");
    }

    public boolean injectPack() {
        return this.general().getBoolean("inject-pack", true);
    }

    public boolean devMode() {
        return this.general().getBoolean("dev-mode", false);
    }

    // Pack
    public String packName() {
        return this.pack().getString("name");
    }

    public String packDescription() {
        return this.pack().getString("description");
    }

    public Toml pack() {
        return this.toml.getTable("pack");
    }
}
