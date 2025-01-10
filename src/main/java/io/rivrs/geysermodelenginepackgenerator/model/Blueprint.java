package io.rivrs.geysermodelenginepackgenerator.model;

import io.rivrs.creativebedrock.animation.ActorAnimation;
import io.rivrs.creativebedrock.geometry.Geometry;
import java.nio.file.Path;
import org.jetbrains.annotations.Nullable;

public record Blueprint(String id, Path path, @Nullable BlueprintSettings settings, Path texturePath, Geometry geometry,
                        ActorAnimation animation) {
}
