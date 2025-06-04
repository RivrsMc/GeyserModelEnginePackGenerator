package re.imc.geysermodelenginepackgenerator.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import re.imc.geysermodelenginepackgenerator.ExtensionMain;

public class VersionCounter {

    private final Path path;
    @Getter
    private final List<Long> version = new ArrayList<>();

    public VersionCounter(ExtensionMain extension) {
        this.path = extension.dataFolder().resolve("version");
    }

    public void load() {
        if (Files.exists(path)) {
            try {
                String content = Files.readString(path);
                String[] parts = content.split(",");
                for (String part : parts) {
                    version.add(Long.parseLong(part));
                }
            } catch (Exception e) {
                ExtensionMain.logger.error("Failed to load version counter", e);
            }
        } else {
            setVersion(1, 0, 0);
        }
    }

    public void save() {
        if (!Files.exists(path.getParent())) {
            try {
                Files.createDirectories(path.getParent());
            } catch (java.io.IOException e) {
                ExtensionMain.logger.error("Failed to create directories for version counter", e);
                return;
            }
        }

        String versionString = String.join(",", version.stream().map(String::valueOf).toList());
        try {
            Files.writeString(path, versionString);
        } catch (java.io.IOException e) {
            ExtensionMain.logger.error("Failed to save version counter", e);
        }
    }

    public void setVersion(long major, long minor, long patch) {
        version.clear();
        version.add(major);
        version.add(minor);
        version.add(patch);
        save();
    }

    public void increase(boolean save) {
        if (version.isEmpty()) {
            setVersion(1, 0, 0);
        } else {
            long major = version.get(0);
            long minor = version.get(1);
            long patch = version.get(2);

            patch++;
            if (patch >= 100) {
                patch = 0;
                minor++;
                if (minor >= 100) {
                    minor = 0;
                    major++;
                }
            }

            version.clear();
            version.add(major);
            version.add(1, minor);
            version.add(2, patch);
        }

        if (save)
            save();
    }
}
