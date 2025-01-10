package io.rivrs.geysermodelenginepackgenerator;

import io.rivrs.geysermodelenginepackgenerator.blueprint.BlueprintManager;
import lombok.Getter;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;

@Getter
public class ExtensionMain implements Extension {

    private BlueprintManager blueprints;

    @Subscribe
    public void onLoad(GeyserPreInitializeEvent e) {
        // Blueprints
        this.blueprints = new BlueprintManager(this);
        this.blueprints.load();
    }


}
