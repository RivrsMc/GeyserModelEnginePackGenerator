package re.imc.geysermodelenginepackgenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;
import lombok.Getter;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserLoadResourcePacksEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.extension.ExtensionLogger;
import re.imc.geysermodelenginepackgenerator.configuration.Configuration;
import re.imc.geysermodelenginepackgenerator.generator.Entity;
import re.imc.geysermodelenginepackgenerator.util.ZipUtil;

public class ExtensionMain implements Extension {

    private static ExtensionMain instance;

    private File source;
    public static ExtensionLogger logger;
    private Path generatedPackZip;
    @Getter
    private Configuration configuration;

    @Subscribe
    public void onLoad(GeyserPreInitializeEvent event) {
        this.configuration = new Configuration(this);

        source = dataFolder().resolve("input").toFile();
        source.mkdirs();
        logger = logger();

        instance = this;

        loadConfig();

    }

    @Subscribe
    public void onDefineCommand(GeyserDefineCommandsEvent event) {
        event.register(Command.builder(this)
                .name("reload")
                .source(CommandSource.class)
                .playerOnly(false)
                .description("GeyserModelPackGenerator Reload Command")
                .permission("geysermodelenginepackgenerator.admin")
                .executor((source, command, args) -> {
                    this.configuration.load();
                    loadConfig();
                    source.sendMessage("GeyserModelEnginePackGenerator reloaded!");
                })
                .build());
    }

    public void loadConfig() {
        File generatedPack = dataFolder().resolve("generated_pack").toFile();
        GeneratorMain.startGenerate(source, generatedPack);
        generatedPackZip = dataFolder().resolve("generated_pack.zip");

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(generatedPackZip))) {
            ZipUtil.compressFolder(generatedPack, null, zipOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Entity entity : GeneratorMain.entityMap.values()) {
            entity.register();
        }

    }

    @Subscribe
    public void onPackLoad(GeyserLoadResourcePacksEvent event) {
        if (this.configuration.isAutoLoad()) {
            event.resourcePacks().add(generatedPackZip);
        }
    }

    public static ExtensionMain get() {
        return instance;
    }


}
