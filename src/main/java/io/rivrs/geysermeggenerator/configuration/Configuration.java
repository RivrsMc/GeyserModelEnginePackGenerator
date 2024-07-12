package io.rivrs.geysermeggenerator.configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

    // Pack
    public String packName() {
        return this.pack().getString("name");
    }

    public List<Long> packVersion() {
        return this.pack().getList("version", new ArrayList<>());
    }

    public String packDescription() {
        return this.pack().getString("description");
    }

    public Toml pack() {
        return this.toml.getTable("pack");
    }
}
