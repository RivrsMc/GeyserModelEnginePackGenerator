package io.rivrs.geysermeggenerator;

import java.nio.file.Path;

import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.lifecycle.GeyserLoadResourcePacksEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.rivrs.geysermeggenerator.cache.CacheManager;
import io.rivrs.geysermeggenerator.cache.CacheType;
import io.rivrs.geysermeggenerator.configuration.Configuration;
import io.rivrs.geysermeggenerator.configuration.adapter.PathTypeAdapter;
import io.rivrs.geysermeggenerator.generator.GeneratorManager;
import io.rivrs.geysermeggenerator.utils.FileUtils;
import lombok.Getter;

@Getter
public class ExtensionMain implements Extension {

    // Gson
    public static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Path.class, new PathTypeAdapter())
            .setPrettyPrinting()
            .create();

    // Folders
    private Path inputFolder;
    private Path outputFolder;
    private Path packPath;
    private Path cacheFolder;
    private Path resourcePackPath;

    // Configuration
    private Configuration configuration;

    // Managers
    private CacheManager cacheManager;
    private GeneratorManager generatorManager;

    @Subscribe
    public void onLoad(GeyserPreInitializeEvent event) {
        logger().info("Loading ModelEngine pack generator extension...");
        long start = System.currentTimeMillis();

        // Paths
        this.inputFolder = dataFolder().resolve("input");
        this.outputFolder = dataFolder().resolve("output");
        this.packPath = dataFolder().resolve("modelengine.mcpack");
        this.cacheFolder = dataFolder().resolve(".cache");
        this.resourcePackPath = dataFolder().resolve("generated_pack.mcpack");

        // Create data folders
        FileUtils.createDirectories(inputFolder, outputFolder, cacheFolder);

        // Configuration
        this.configuration = new Configuration(dataFolder().resolve("config.toml"));
        this.configuration.load();

        // Cache
        this.cacheManager = new CacheManager(this, cacheFolder);
        this.cacheManager.load();

        // Convert
        this.generatorManager = new GeneratorManager(this);
        if (this.cacheManager.hasChanged())
            this.generatorManager.generate();
        else
            logger().info("No changes detected, skipping generation.");

        // Export
        this.cacheManager.export(CacheType.FILE_SYSTEM);

        logger().info("ModelEngine pack generator extension loaded in " + (System.currentTimeMillis() - start) + "ms.");

//        generatedPackZip = dataFolder().resolve("generated_pack.zip");
//
//        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(generatedPackZip))) {
//            // 压缩文件夹
//            ZipUtil.compressFolder(generatedPack, null, zipOutputStream);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for (String entity : GeneratorMain.entityMap.keySet()) {
//            String id = "modelengine:" + entity;
//            GeyserUtils.addCustomEntity(id);
//
//            Geometry geometry = GeneratorMain.geometryMap.get(entity);
//            geometry.getBones().forEach(bone -> {
//                GeyserUtils.addProperty(id, entity + ":" + bone, Boolean.class);
//            });
//
//            GeyserUtils.addProperty(id, "modelengine:anim_idle", Boolean.class);
//            GeyserUtils.addProperty(id, "modelengine:anim_spawn", Boolean.class);
//            GeyserUtils.addProperty(id, "modelengine:anim_walk", Boolean.class);
//            GeyserUtils.addProperty(id, "modelengine:anim_stop", Boolean.class);
//
//            GeyserUtils.registerProperties(id);
//        }
    }

    @Subscribe
    public void onPackLoad(GeyserLoadResourcePacksEvent event) {
        if (Boolean.parseBoolean(System.getProperty("geyser-model-engine-auto-load-pack", "true"))) {
            //  event.resourcePacks().add(generatedPackZip);
        }
    }
}
