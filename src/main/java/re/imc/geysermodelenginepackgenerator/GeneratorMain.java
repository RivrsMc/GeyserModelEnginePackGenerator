package re.imc.geysermodelenginepackgenerator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.googlecode.pngtastic.core.PngImage;
import com.googlecode.pngtastic.core.PngOptimizer;

import re.imc.geysermodelenginepackgenerator.generator.*;
import re.imc.geysermodelenginepackgenerator.model.ContentTable;
import re.imc.geysermodelenginepackgenerator.model.VersionCounter;

public class GeneratorMain {

    public static final Map<String, Entity> entityMap = new HashMap<>();
    public static final Map<String, Animation> animationMap = new HashMap<>();
    public static final Map<String, Geometry> geometryMap = new HashMap<>();
    public static final Map<String, Map<String, Texture>> textureMap = new HashMap<>();
    public static final Gson GSON = new Gson();

    public static void generateFromZip(String currentPath, String modelId, ZipFile zip) {
        Entity entity = new Entity(modelId);
        if (entityMap.containsKey(modelId)) {
            return;
        }
        ModelConfig modelConfig = new ModelConfig();
        ZipEntry textureConfigFile = null;
        for (Iterator<? extends ZipEntry> it = zip.entries().asIterator(); it.hasNext(); ) {
            ZipEntry entry = it.next();
            if (entry.getName().endsWith("config.json")) {
                textureConfigFile = entry;
            }
        }

        if (textureConfigFile != null) {
            try {
                modelConfig = GSON.fromJson(new InputStreamReader(zip.getInputStream(textureConfigFile)), ModelConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        boolean canAdd = false;
        for (Iterator<? extends ZipEntry> it = zip.entries().asIterator(); it.hasNext(); ) {
            ZipEntry e = it.next();
            if (e.getName().endsWith(".png")) {
                String[] path = e.getName().split("/");
                String textureName = path[path.length - 1].replace(".png", "");
                Set<String> bindingBones = new HashSet<>();
                bindingBones.add("*");
                if (modelConfig.getBingingBones().containsKey(textureName)) {
                    bindingBones = modelConfig.getBingingBones().get(textureName);
                }
                Map<String, Texture> map = textureMap.computeIfAbsent(modelId, s -> new HashMap<>());
                try {
                    map.put(textureName, new Texture(modelId, currentPath, bindingBones, zip.getInputStream(e).readAllBytes()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                entity.setTextureMap(map);
                if (modelConfig.getBingingBones().isEmpty()) {
                    modelConfig.getBingingBones().put(textureName, Set.of("*"));
                }

            }
            if (e.getName().endsWith(".json")) {
                try {
                    InputStream stream = zip.getInputStream(e);
                    String json = new String(stream.readAllBytes());
                    if (isAnimationFile(json)) {
                        Animation animation = new Animation();
                        animation.setPath(currentPath);
                        animation.setModelId(modelId);

                        animation.load(json);
                        animationMap.put(modelId, animation);
                        entity.setAnimation(animation);
                    }

                    if (isGeometryFile(json)) {
                        Geometry geometry = new Geometry();
                        geometry.load(json);
                        geometry.setPath(currentPath);
                        geometry.setModelId(modelId);
                        geometryMap.put(modelId, geometry);
                        entity.setGeometry(geometry);
                        canAdd = true;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (canAdd) {
            entity.setModelConfig(modelConfig);
            entity.setPath(currentPath);
            entityMap.put(modelId, entity);
        }
    }

    public static void generateFromFolder(String currentPath, File folder, boolean root) {
        if (folder.listFiles() == null) {
            return;
        }
        String modelId = root ? "" : folder.getName().toLowerCase();

        Entity entity = new Entity(modelId);
        ModelConfig modelConfig = new ModelConfig();
        boolean shouldOverrideConfig = false;
        File textureConfigFile = new File(folder, "config.json");
        if (textureConfigFile.exists()) {
            try {
                modelConfig = GSON.fromJson(Files.readString(textureConfigFile.toPath()), ModelConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        boolean canAdd = false;
        for (File e : folder.listFiles()) {
            if (e.isDirectory()) {
                generateFromFolder(currentPath + (root ? "" : folder.getName() + "/"), e, false);
            }
            if (e.getName().endsWith(".zip")) {
                try {
                    generateFromZip(currentPath, e.getName().replace(".zip", "").toLowerCase(Locale.ROOT), new ZipFile(e));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (entityMap.containsKey(modelId)) {
                continue;
            }
            if (e.getName().endsWith(".png")) {
                String textureName = e.getName().replace(".png", "");
                Set<String> bindingBones = new HashSet<>();
                bindingBones.add("*");
                if (modelConfig.getBingingBones().containsKey(textureName)) {
                    bindingBones = modelConfig.getBingingBones().get(textureName);
                }
                Map<String, Texture> map = textureMap.computeIfAbsent(modelId, s -> new HashMap<>());
                try {
                    map.put(textureName, new Texture(modelId, currentPath, bindingBones, Files.readAllBytes(e.toPath())));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                entity.setTextureMap(map);
                if (modelConfig.getBingingBones().isEmpty()) {
                    modelConfig.getBingingBones().put(textureName, Set.of("*"));
                    shouldOverrideConfig = true;
                }

            }
            if (e.getName().endsWith(".json")) {
                try {
                    String json = Files.readString(e.toPath());
                    if (isAnimationFile(json)) {
                        Animation animation = new Animation();
                        animation.setPath(currentPath);
                        animation.setModelId(modelId);

                        animation.load(json);
                        animationMap.put(modelId, animation);
                        entity.setAnimation(animation);
                    }

                    if (isGeometryFile(json)) {
                        Geometry geometry = new Geometry();
                        geometry.load(json);
                        geometry.setPath(currentPath);
                        geometry.setModelId(modelId);
                        geometryMap.put(modelId, geometry);
                        entity.setGeometry(geometry);
                        canAdd = true;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (canAdd) {
            // old config
            File oldConfig = new File(folder, "config.properties");
            Properties old = new Properties();
            try {
                if (oldConfig.exists()) {
                    old.load(new FileReader(oldConfig));
                    modelConfig.setMaterial(old.getProperty("material", "entity_alphatest_change_color"));
                    modelConfig.setEnableBlendTransition(Boolean.parseBoolean(old.getProperty("blend-transition", "true")));
                    modelConfig.setEnableHeadRotation(Boolean.parseBoolean(old.getProperty("head-rotation", "true")));
                    shouldOverrideConfig = true;
                    oldConfig.delete();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (shouldOverrideConfig) {
                try {
                    Files.writeString(textureConfigFile.toPath(), GSON.toJson(modelConfig));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            entity.setModelConfig(modelConfig);
            entity.setPath(currentPath);
            entityMap.put(modelId, entity);
        }
    }

    public static void startGenerate(File source, File output, boolean generate) {
        VersionCounter versionCounter = new VersionCounter(ExtensionMain.get());
        versionCounter.load();
        if (generate)
            versionCounter.increase(true);

        generateFromFolder("", source, true);

        File animationsFolder = new File(output, "animations");
        File entityFolder = new File(output, "entity");
        File modelsFolder = new File(output, "models/entity");
        File texturesFolder = new File(output, "textures/entity");
        File animationControllersFolder = new File(output, "animation_controllers");
        File renderControllersFolder = new File(output, "render_controllers");
        File materialsFolder = new File(output, "materials");
        File manifestFile = new File(output, "manifest.json");

        Path outputDirectory = output.toPath();

        output.mkdirs();

        // Manifest
        if (!manifestFile.exists()) {
            try {

                Files.writeString(manifestFile.toPath(),
                        PackManifest.generate(versionCounter), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Icon
        Path iconPath = ExtensionMain.get().dataFolder().resolve("icon.png");
        if (Files.exists(iconPath) && !new File(output, "pack_icon.png").exists()) {
            try {
                Files.copy(iconPath, new File(output, "pack_icon.png").toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Create folders
        animationsFolder.mkdirs();
        entityFolder.mkdirs();
        modelsFolder.mkdirs();
        texturesFolder.mkdirs();
        animationControllersFolder.mkdirs();
        renderControllersFolder.mkdirs();
        materialsFolder.mkdirs();

        ContentTable contentTable = new ContentTable(new ArrayList<>());

        // Material file
        File materialFile = new File(materialsFolder, "entity.material");
        if (!materialFile.exists()) {
            try {
                Files.writeString(materialFile.toPath(),
                        Material.TEMPLATE, StandardCharsets.UTF_8);

                contentTable.add(outputDirectory.relativize(materialFile.toPath()).toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Animation
        for (Map.Entry<String, Animation> entry : animationMap.entrySet()) {
            Entity entity = entityMap.get(entry.getKey());
            Geometry geo = geometryMap.get(entry.getKey());
            if (geo != null) {
                entry.getValue().addHeadBind(geo);
            }
            Path path = animationsFolder.toPath().resolve(entry.getValue().getPath() + entry.getKey() + ".animation.json");
            Path pathController = animationControllersFolder.toPath().resolve(entry.getValue().getPath() + entry.getKey() + ".animation_controllers.json");

            pathController.toFile().getParentFile().mkdirs();
            path.toFile().getParentFile().mkdirs();

            if (path.toFile().exists()) {
                continue;
            }

            contentTable.add(outputDirectory.relativize(path).toString());
            contentTable.add(outputDirectory.relativize(pathController).toString());

            AnimationController controller = new AnimationController();
            controller.load(entry.getValue(), entity);

            try {
                Files.writeString(path, GSON.toJson(entry.getValue().getJson()), StandardCharsets.UTF_8);
                Files.writeString(pathController, controller.getJson().toString(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Geometry
        for (Map.Entry<String, Geometry> entry : geometryMap.entrySet()) {
            entry.getValue().modify();
            Path path = modelsFolder.toPath().resolve(entry.getValue().getPath() + entry.getKey() + ".geo.json");
            path.toFile().getParentFile().mkdirs();
            String id = entry.getValue().getGeometryId();

            Entity entity = entityMap.get(entry.getKey());
            if (entity != null) {
                ModelConfig modelConfig = entity.getModelConfig();
                if (!modelConfig.getPerTextureUvSize().isEmpty()) {
                    for (Map.Entry<String, Texture> textureEntry : entity.getTextureMap().entrySet()) {
                        String name = textureEntry.getKey();

                        Integer[] size = modelConfig.getPerTextureUvSize().getOrDefault(name, new Integer[]{16, 16});
                        String suffix = size[0] + "_" + size[1];
                        entry.getValue().setTextureWidth(size[0]);
                        entry.getValue().setTextureHeight(size[1]);
                        path = modelsFolder.toPath().resolve(entry.getValue().getPath() + entry.getKey() + "_" + suffix + ".geo.json");

                        entry.getValue().setId(id + "_" + suffix);

                        if (path.toFile().exists()) {
                            continue;
                        }

                        contentTable.add("models/entity/" + entry.getValue().getPath() + entry.getKey() + "_" + suffix + ".geo.json");

                        try {
                            Files.writeString(path, GSON.toJson(entry.getValue().getJson()), StandardCharsets.UTF_8);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

            if (path.toFile().exists()) {
                continue;
            }

            contentTable.add(outputDirectory.relativize(path).toString());

            try {
                Files.writeString(path, GSON.toJson(entry.getValue().getJson()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Textures
        PngOptimizer pngOptimizer = new PngOptimizer();
        List<String> texturesList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Texture>> textures : textureMap.entrySet()) {
            for (Map.Entry<String, Texture> entry : textures.getValue().entrySet()) {
                Path path = texturesFolder.toPath().resolve(entry.getValue().getPath() + textures.getKey() + "/" + entry.getKey() + ".png");
                path.toFile().getParentFile().mkdirs();
                if (Files.exists(path))
                    continue;

                try {
                    if (entry.getValue().getImage() != null) {
                        if (ExtensionMain.get().getConfiguration().getGeneral().optimizeTextures()) {
                            PngImage pngImage = new PngImage(entry.getValue().getImage());
                            PngImage optimized = pngOptimizer.optimize(pngImage, false, 12);

                            Files.createFile(path);
                            try (OutputStream outputStream = Files.newOutputStream(path)) {
                                optimized.writeDataOutputStream(outputStream);
                            }
                        } else {
                            Files.write(path, entry.getValue().getImage());
                        }

                        String raw = outputDirectory.relativize(path).toString();
                        texturesList.add(raw);
                        contentTable.add(raw);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Textures list
        Path texturesListPath = texturesFolder.toPath().getParent().resolve("textures_list.json");
        if (!Files.exists(texturesListPath)) {
            try {
                Files.createFile(texturesListPath);
                Files.writeString(texturesListPath, GSON.toJson(texturesList), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write textures list file", e);
            }
        }

        // Entity
        for (Map.Entry<String, Entity> entry : entityMap.entrySet()) {
            Entity entity = entry.getValue();
            entity.modify();

            Path entityPath = entityFolder.toPath().resolve(entity.getPath() + entry.getKey() + ".entity.json");
            entityPath.toFile().getParentFile().mkdirs();
            if (entityPath.toFile().exists()) {
                continue;
            }
            try {
                Files.writeString(entityPath, entity.getJson().toString(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // render controller part

            String id = entity.getModelId();
            if (!geometryMap.containsKey(id)) continue;
            RenderController controller = new RenderController(id, geometryMap.get(id).getBones(), entity);
            entity.setRenderController(controller);
            Path renderPath = new File(renderControllersFolder, "controller.render." + id + ".json").toPath();
            if (renderPath.toFile().exists()) {
                continue;
            }

            contentTable.add(outputDirectory.relativize(renderPath).toString());

            try {
                Files.writeString(renderPath, controller.generate(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Content table
        Path contentTablePath = output.toPath().resolve("contents.json");
        if (!Files.exists(contentTablePath)) {
            try {
                Files.createFile(contentTablePath);
                Files.writeString(contentTablePath, GSON.toJson(contentTable), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write content table file", e);
            }
        }
    }

    private static boolean isGeometryFile(String json) {
        try {
            return JsonParser.parseString(json).getAsJsonObject().has("minecraft:geometry");
        } catch (Throwable e) {
            return false;
        }
    }

    private static boolean isAnimationFile(String json) {
        try {
            return JsonParser.parseString(json).getAsJsonObject().has("animations");
        } catch (Throwable e) {
            return false;
        }
    }

}