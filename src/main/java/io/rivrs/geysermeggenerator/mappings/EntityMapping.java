package io.rivrs.geysermeggenerator.mappings;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class EntityMapping {

    private final String modelId;
    private final List<String> bones = new ArrayList<>();
    private final List<String> animations = new ArrayList<>();

    public void addBones(List<String> bones) {
        this.bones.addAll(bones);
    }

    public void addAnimation(String animation) {
        animations.add(animation);
    }
}
