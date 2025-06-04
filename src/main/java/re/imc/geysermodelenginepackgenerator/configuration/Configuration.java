package re.imc.geysermodelenginepackgenerator.configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.moandjiezana.toml.Toml;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Configuration {

    private final Path path;
    private Toml toml;

    @Getter
    private Pack pack;
    @Getter
    private General general;

    public void load() {
        this.createDefault();

        this.toml = new Toml().read(path.toFile());
        if (this.toml == null)
            throw new RuntimeException("Failed to load configuration from: " + path);

        if (!this.toml.contains("pack"))
            throw new RuntimeException("Configuration file does not contain 'pack' section: " + path);

        Toml packSection = this.toml.getTable("pack");
        if (packSection == null)
            throw new RuntimeException("Configuration file does not contain 'pack' section: " + path);

        String name = packSection.getString("name");
        String description = packSection.getString("description");
        List<Long> version = packSection.getList("version");

        if (name == null || description == null || version == null || version.size() != 3)
            throw new RuntimeException("Invalid 'pack' section in configuration file: " + path);

        this.pack = new Pack(name, description, version);

        if (!this.toml.contains("general"))
            throw new RuntimeException("Configuration file does not contain 'general' section: " + path);

        Toml generalSection = this.toml.getTable("general");
        if (generalSection == null)
            throw new RuntimeException("Configuration file does not contain 'general' section: " + path);

        boolean injectPack = generalSection.getBoolean("inject-pack", true);
        boolean optimizeTextures = generalSection.getBoolean("optimize-textures", true);

        this.general = new General(injectPack, optimizeTextures);

    }

    private void createDefault() {
        Path parent = path.getParent();
        if (!Files.isDirectory(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create configuration directory: " + parent, e);
            }
        }

        if (Files.exists(path))
            return;

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.toml")) {
            if (inputStream == null) {
                throw new RuntimeException("Default configuration file not found in resources.");
            }
            Files.copy(inputStream, path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create default configuration file: " + path, e);

        }
    }

    public record General(boolean injectPack, boolean optimizeTextures) {

    }

    public record Pack(String name, String description, List<Long> version) {
    }
}
