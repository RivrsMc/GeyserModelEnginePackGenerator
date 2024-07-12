package io.rivrs.geysermeggenerator.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.Blocking;

import com.google.gson.JsonParser;

import io.rivrs.geysermeggenerator.ExtensionMain;
import io.rivrs.geysermeggenerator.configuration.Configuration;
import io.rivrs.geysermeggenerator.model.*;
import io.rivrs.geysermeggenerator.utils.FileUtils;
import io.rivrs.geysermeggenerator.utils.ZipUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GeneratorManager {

    private final ExtensionMain extension;
    private final Map<String, Entity> entityMap = new HashMap<>();
    private final Map<String, Animation> animationMap = new HashMap<>();
    private final Map<String, Geometry> geometryMap = new HashMap<>();
    private final Map<String, Texture> textureMap = new HashMap<>();

    @Blocking
    public void generate() {
        // Load
        this.load();

        // Generate
        this.generatePack();
    }

    private void load() {
        long start = System.currentTimeMillis();
        try (Stream<Path> stream = Files.walk(this.extension.getInputFolder())) {
            stream.filter(Files::isRegularFile)
                    .forEach(path -> {
                        String modelId = path.getParent().getFileName().toString().toLowerCase();
                        String fileName = path.getFileName().toString().toLowerCase();
                        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

                        // Textures
                        if (extension.equalsIgnoreCase("png")) {
                            this.textureMap.put(modelId, new Texture(modelId, path));
                        }
                        // Geometry & animations
                        else if (extension.equalsIgnoreCase("json")) {
                            try {
                                String json = Files.readString(path);

                                if (isGeometry(json)) {
                                    this.geometryMap.put(modelId, new Geometry(modelId, path, json));
                                } else if (isAnimation(json)) {
                                    this.animationMap.put(modelId, new Animation(modelId, path, json));
                                } else {
                                    this.extension.logger().warning("File: " + path + " is not a valid geometry or animation file");
                                }
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to load file at " + path, e);
                            }
                        } else {
                            this.extension.logger().warning("Unknown file type: " + path);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ModelEngine pack", e);
        }

        // Group entities
        for (String modelId : textureMap.keySet()) {
            boolean hasGeometry = geometryMap.containsKey(modelId);

            if (!hasGeometry) {
                this.extension.logger().warning("Model " + modelId + " is missing geometry");
                continue;
            }

            Path path = geometryMap.get(modelId).getPath();
            this.entityMap.put(modelId, new Entity(modelId, path));
        }

        this.extension.logger().info("Loaded " + this.entityMap.size() + " entities (Took " + (System.currentTimeMillis() - start) + "ms)");
    }

    private void generatePack() {
        this.extension.logger().info("Generating ModelEngine pack...");
        long start = System.currentTimeMillis();

        // Paths
        Path output = this.extension.getOutputFolder();
        Path animationsFolder = output.resolve("animations");
        Path entityFolder = output.resolve("entity");
        Path modelsFolder = output.resolve("models/entity");
        Path texturesFolder = output.resolve("textures/entity");
        Path animationControllersFolder = output.resolve("animation_controllers");
        Path renderControllersFolder = output.resolve("render_controllers");

        // Create folders
        FileUtils.createDirectories(output);

        // Manifest
        this.generateManifest(output.resolve("manifest.json"));

        // Animations
        this.generateAnimations(animationsFolder);

        // Geometries
        this.generateGeometries(modelsFolder);

        // Entities
        this.generateEntities(entityFolder);

        // Render controllers
        this.generateRenderControllers(renderControllersFolder);

        // Animation controllers
        this.generateAnimationsControllers(animationControllersFolder);

        // Textures
        this.exportTextures(texturesFolder);

        // Zip pack
        try {
            ZipUtils.zipFolder(output, this.extension.getResourcePackPath());
            FileUtils.deleteFolders(output);
        } catch (IOException e) {
            throw new RuntimeException("Failed to zip ModelEngine pack", e);
        }

        this.extension.logger().info("ModelEngine pack generated in " + (System.currentTimeMillis() - start) + "ms. (Took " + (System.currentTimeMillis() - start) + "ms)");
    }

    private void generateManifest(Path path) {
        // Delete if exists
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete manifest file", e);
        }

        // Generate & export
        try {
            Files.createDirectories(path.getParent());
            Configuration configuration = this.extension.getConfiguration();
            String rawManifest = PackManifest.generate(configuration.packName(), configuration.packDescription(), this.extension.getCacheManager().getAndIncrementVersion());
            Files.writeString(path, rawManifest, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export manifest file", e);
        }
    }

    private void generateAnimations(Path dataFolder) {
        for (Map.Entry<String, Animation> entry : this.animationMap.entrySet()) {
            String modelId = entry.getKey();
            Animation animation = entry.getValue();

            // Get entity
            Entity entity = this.entityMap.get(modelId);
            if (entity == null) {
                this.extension.logger().warning("Entity " + modelId + " is missing geometry");
                continue;
            }

            // Alter animation
            animation.modify(entity);

            // Link geometry
            Geometry geometry = this.geometryMap.get(modelId);
            if (geometry != null)
                animation.addHeadBind(entity, geometry);

            // Export
            Path relativizedPath = this.extension.getInputFolder().relativize(animation.getPath().getParent().getParent());
            Path path = dataFolder.resolve(relativizedPath.resolve(modelId + ".animation.json"));

            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, ExtensionMain.GSON.toJson(animation.getJson()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Failed to export animation file at " + path, e);
            }
        }
    }

    private void generateGeometries(Path dataFolder) {
        for (Map.Entry<String, Geometry> entry : this.geometryMap.entrySet()) {
            String modelId = entry.getKey();
            Geometry geometry = entry.getValue();

            // Alter geometry
            geometry.modify();

            // Export
            Path relativizedPath = this.extension.getInputFolder().relativize(geometry.getPath().getParent().getParent());
            Path path = dataFolder.resolve(relativizedPath.resolve(modelId + ".geo.json"));

            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, ExtensionMain.GSON.toJson(geometry.getJson()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Failed to export geometry file at " + path, e);
            }
        }
    }

    private void exportTextures(Path dataFolder) {
        for (Map.Entry<String, Texture> entry : this.textureMap.entrySet()) {
            String modelId = entry.getKey();
            Texture texture = entry.getValue();

            Path relativizedPath = this.extension.getInputFolder().relativize(texture.path().getParent().getParent());
            Path path = dataFolder.resolve(relativizedPath.resolve(modelId + ".png"));

            try {
                Files.createDirectories(path.getParent());
                Files.copy(texture.path(), path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to export texture file at " + path, e);
            }
        }
    }

    private void generateEntities(Path dataFolder) {
        for (Map.Entry<String, Entity> entry : entityMap.entrySet()) {
            String modelId = entry.getKey();
            Entity entity = entry.getValue();

            // Alter
            entity.getProperties().setProperty("render_controller", "controller.render." + modelId);
            entity.modify(entity.getPath().getParent().getFileName().toString());

            // Export
            Path relativizedPath = this.extension.getInputFolder().relativize(entity.getPath().getParent().getParent());
            Path path = dataFolder.resolve(relativizedPath.resolve(modelId + ".entity.json"));

            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, entity.getJson(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Failed to export entity file at " + path, e);
            }
        }
    }

    private void generateRenderControllers(Path dataFolder) {
        for (Map.Entry<String, Entity> entry : entityMap.entrySet()) {
            String modelId = entry.getKey();
            Entity entity = entry.getValue();

            // Get geometry
            Geometry geometry = geometryMap.get(modelId);
            if (geometry == null) {
                this.extension.logger().warning("Entity " + modelId + " is missing geometry");
                continue;
            }

            // Create render controller
            RenderController renderController = new RenderController(modelId, geometry.getBones());

            // Export
            Path path = dataFolder.resolve("controller.render." + modelId + ".json");
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, renderController.generate(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Failed to export render controller file at " + path, e);
            }
        }
    }

    private void generateAnimationsControllers(Path dataFolder) {

        // Export
        Path path = dataFolder.resolve("modelengine.animation_controller.json");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, AnimationController.TEMPLATE, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export animation controller file at " + path, e);
        }
    }

    public boolean isGeometry(String json) {
        try {
            return JsonParser.parseString(json).getAsJsonObject().has("minecraft:geometry");
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean isAnimation(String json) {
        try {
            return JsonParser.parseString(json).getAsJsonObject().has("animations");
        } catch (Throwable e) {
            return false;
        }
    }
}
