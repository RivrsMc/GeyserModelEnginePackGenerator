package io.rivrs.geysermodelenginepackgenerator.blueprint;

import io.rivrs.creativebedrock.animation.ActorAnimation;
import io.rivrs.creativebedrock.geometry.Geometry;
import io.rivrs.creativebedrock.serializer.adapter.animation.ActorAnimationSerializer;
import io.rivrs.creativebedrock.serializer.adapter.geometry.GeometrySerializer;
import io.rivrs.geysermodelenginepackgenerator.ExtensionMain;
import io.rivrs.geysermodelenginepackgenerator.model.Blueprint;
import io.rivrs.geysermodelenginepackgenerator.model.BlueprintSettings;
import io.rivrs.geysermodelenginepackgenerator.utils.PathUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Unmodifiable;

@RequiredArgsConstructor
public class BlueprintManager {

    private final Set<Blueprint> blueprints = new HashSet<>();
    private final ExtensionMain extensionMain;

    public void load() {
        Path input = extensionMain.dataFolder().resolve("input");
        extensionMain.logger().info("Loading blueprints...");
        try (Stream<Path> pathStream = Files.walk(input)) {
            pathStream.filter(Files::isDirectory)
                    .filter(path -> !PathUtils.onlyContainsFolders(path))
                    .map(this::loadBlueprint)
                    .filter(Objects::nonNull)
                    .forEach(blueprints::add);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load blueprints", e);
        }
        extensionMain.logger().info("Loaded " + blueprints.size() + " blueprints");
    }

    private Blueprint loadBlueprint(Path path) {
        String id = path.getFileName().toString();

        BlueprintSettings settings = null;
        Path texture = null;
        Geometry geometry = null;
        ActorAnimation animation = null;
        try (Stream<Path> pathStream = Files.walk(path)) {
            List<Path> paths = pathStream.filter(Files::isRegularFile)
                    .toList();

            for (Path p : paths) {
                String name = p.getFileName().toString();
                if (name.endsWith(".png")) {
                    texture = p;
                } else if (name.endsWith(".geo.json")) {
                    try (InputStream inputStream = Files.newInputStream(p)){
                        geometry = GeometrySerializer.INSTANCE.deserialize(inputStream, Key.key("modelengine", id.toLowerCase()));
                    }
                } else if (name.endsWith(".animation.json")) {
                    try (InputStream inputStream = Files.newInputStream(p)){
                        animation = ActorAnimationSerializer.INSTANCE.deserialize(inputStream, Key.key("modelengine", id.toLowerCase()));
                    }
                } else if (name.equalsIgnoreCase("config.json")) {
                    // TODO: parse settings
                }
            }

            if (texture == null
                || geometry == null) {
                String missingFiles = "";
                if (texture == null)
                    missingFiles += "texture, ";
                if (geometry == null)
                    missingFiles += "geometry, ";
                extensionMain.logger().warning("Failed to load blueprint with id: " + id + " due to missing files: " + missingFiles);
                return null;
            }

            return new Blueprint(id, path, settings, texture, geometry, animation);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load blueprint with id: " + id, e);
        }
    }

    @Unmodifiable
    public Set<Blueprint> blueprints() {
        return Set.copyOf(blueprints);
    }
}
