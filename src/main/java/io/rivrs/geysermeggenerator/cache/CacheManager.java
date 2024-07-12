package io.rivrs.geysermeggenerator.cache;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.google.gson.reflect.TypeToken;

import io.rivrs.geysermeggenerator.ExtensionMain;
import io.rivrs.geysermeggenerator.configuration.VersionConfiguration;
import io.rivrs.geysermeggenerator.utils.FileUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CacheManager {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".json", ".png", ".jpeg");

    private final ExtensionMain extension;
    private final Path dataFolder;
    private final List<Cache> caches = new ArrayList<>();
    @Getter
    private final List<CachedEntity> cachedEntities = new ArrayList<>();

    private VersionConfiguration versionConfiguration;

    public void load() {
        this.versionConfiguration = new VersionConfiguration(this.dataFolder.resolve("version.toml"));
        this.versionConfiguration.load();

        loadFromDisk();
        loadFromFileSystem();

        this.loadEntities();
    }

    public void setCachedEntities(List<CachedEntity> entities) {
        this.cachedEntities.clear();
        this.cachedEntities.addAll(entities);
    }

    public void loadEntities() {
        Path path = this.dataFolder.resolve("entities.json");
        if (!Files.exists(path)) {
            this.extension.logger().warning("Entities file not found");
            return;
        }

        try {
            TypeToken<List<CachedEntity>> type = new TypeToken<>() {
            };
            this.cachedEntities.addAll(ExtensionMain.GSON.fromJson(Files.newBufferedReader(path), type.getType()));

            this.extension.logger().info("Loaded %d entities from disk".formatted(this.cachedEntities.size()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load entities", e);
        }
    }

    public void exportEntities() {
        Path path = this.dataFolder.resolve("entities.json");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(ExtensionMain.GSON.toJson(this.cachedEntities));
        } catch (IOException e) {
            throw new RuntimeException("Failed to export entities", e);
        }
    }

    public void loadFromDisk() {
        Path path = this.dataFolder.resolve("cache.json");
        if (!Files.exists(path)) {
            this.extension.logger().warning("Cache file not found");
            return;
        }

        try {
            TypeToken<List<CachedFile>> type = new TypeToken<>() {
            };
            List<CachedFile> files = ExtensionMain.GSON.fromJson(Files.newBufferedReader(path), type.getType());
            this.caches.add(new Cache(CacheType.DISK, files));

            this.extension.logger().info("Loaded cache from disk");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load cache", e);
        }
    }

    public void loadFromFileSystem() {
        List<CachedFile> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(this.extension.getInputFolder())) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> ALLOWED_EXTENSIONS.stream().anyMatch(ext -> path.toString().endsWith(ext)))
                    .forEach(path -> {
                        String checksum = FileUtils.checksum(path);
                        if (checksum != null)
                            files.add(new CachedFile(path, checksum));
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to load cache", e);
        }

        this.caches.add(new Cache(CacheType.FILE_SYSTEM, files));
        this.extension.logger().info("Loaded cache from file system");
    }

    public void export(CacheType cacheType) {
        get(cacheType).ifPresentOrElse(cache -> {
            Path path = this.dataFolder.resolve("cache.json");
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(ExtensionMain.GSON.toJson(cache.files()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to export cache", e);
            }
        }, () -> extension.logger().warning("Failed to export cache: " + cacheType.name()));
    }

    public boolean hasChanged() {
        if (!has(CacheType.DISK) || !has(CacheType.FILE_SYSTEM))
            return true;

        Cache disk = get(CacheType.DISK).get();
        Cache fileSystem = get(CacheType.FILE_SYSTEM).get();

        if (disk.files() == null || disk.files().isEmpty()) {
            this.extension.logger().warning("Disk cache is empty");
            return true;
        } else if (fileSystem.files() == null || fileSystem.files().isEmpty()) {
            this.extension.logger().warning("File system cache is empty");
            return true;
        }

        return disk.files().size() != fileSystem.files().size()
               || disk.files()
                       .stream()
                       .anyMatch(file -> fileSystem.files().stream().noneMatch(f -> f.path().equals(file.path()) && f.checksum().equals(file.checksum())));
    }

    public boolean has(CacheType type) {
        return this.caches.stream().anyMatch(cache -> cache.type().equals(type));
    }

    @NotNull
    public Optional<Cache> get(CacheType type) {
        return this.caches.stream()
                .filter(cache -> cache.type().equals(type))
                .findFirst();
    }

    public List<Long> version() {
        return this.versionConfiguration.version();
    }

    public List<Long> getAndIncrementVersion() {
        List<Long> originalVersion = version();

        // Increment
        List<Long> newVersion = new ArrayList<>(originalVersion);
        for (int i = newVersion.size() - 1; i >= 0; i--) {
            long part = newVersion.get(i);
            if (part < 9) {
                newVersion.set(i, part + 1);
                break;
            } else {
                newVersion.set(i, 0L);
            }
        }
        this.versionConfiguration.save(newVersion);

        return originalVersion;
    }
}
