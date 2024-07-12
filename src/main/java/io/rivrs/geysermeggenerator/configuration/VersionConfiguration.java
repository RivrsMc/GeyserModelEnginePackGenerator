package io.rivrs.geysermeggenerator.configuration;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.moandjiezana.toml.Toml;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VersionConfiguration {

    private final Path path;
    private Toml toml;

    public void load() {
        if (!Files.exists(this.path))
            this.loadDefault();

        this.toml = new Toml().read(this.path.toFile());
    }

    public void save(List<Long> version) {
        try {
            String content = "version = [ " + String.join(", ", version.stream().map(Object::toString).toList()) + " ]";
            Files.writeString(this.path, content, StandardCharsets.UTF_8);

            this.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save configuration.", e);
        }
    }

    private void loadDefault() {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("version.toml")) {
            if (inputStream == null)
                throw new RuntimeException("Default configuration not found.");
            Files.copy(inputStream, this.path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load default configuration.", e);
        }
    }

    public List<Long> version() {
        if (this.toml == null)
            throw new RuntimeException("Configuration not loaded.");

        return this.toml.getList("version", List.of(1L, 0L, 0L));
    }
}