package re.imc.geysermodelenginepackgenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;

import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.lifecycle.GeyserLoadResourcePacksEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.extension.ExtensionLogger;

import lombok.Getter;
import re.imc.geysermodelenginepackgenerator.configuration.Configuration;
import re.imc.geysermodelenginepackgenerator.generator.Entity;
import re.imc.geysermodelenginepackgenerator.util.ChecksumUtils;
import re.imc.geysermodelenginepackgenerator.util.ZipUtil;

public class ExtensionMain implements Extension {

    private static ExtensionMain instance;
    public static ExtensionLogger logger;

    private File source;
    private Path generatedPackZip;
    @Getter
    private Configuration configuration;

    @Subscribe
    public void onLoad(GeyserPreInitializeEvent event) {
        this.configuration = new Configuration(dataFolder().resolve("config.toml"));
        this.configuration.load();

        instance = this;

        source = dataFolder().resolve("input").toFile();
        source.mkdirs();

        logger = logger();
        try {
            loadConfig();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public void loadConfig() throws IOException {
        Path propertiesFile = dataFolder().resolve("checksum");

        boolean generate = true;
        String currentChecksum = ChecksumUtils.hashDirectory(source.toPath(), false);
        if (Files.exists(dataFolder().resolve("generated_pack.zip"))) {
            if (Files.exists(propertiesFile)) {
                String lastChecksum = Files.readString(propertiesFile);
                if (lastChecksum.equals(currentChecksum)) {
                    logger.info("No changes detected, skipping regeneration.");
                    generate = false;
                } else {
                    Files.writeString(propertiesFile, currentChecksum);
                    logger.info("Changes detected, regenerating pack.");
                }
            } else {
                Files.createFile(propertiesFile);
                Files.writeString(propertiesFile, currentChecksum);
            }
        }

        File generatedPack = dataFolder().resolve("generated_pack").toFile();
        // if (generate)
        GeneratorMain.startGenerate(source, generatedPack, generate);
        generatedPackZip = dataFolder().resolve("generated_pack.zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(generatedPackZip))) {
            ZipUtil.compressFolder(generatedPack, "meg", zipOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Entity entity : GeneratorMain.entityMap.values()) {
            entity.register();
        }
    }


    @Subscribe
    public void onPackLoad(GeyserLoadResourcePacksEvent event) {
        if (this.configuration.getGeneral().injectPack())
            event.resourcePacks().add(generatedPackZip);
    }

    public static ExtensionMain get() {
        return instance;
    }
}
