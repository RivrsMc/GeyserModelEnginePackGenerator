package io.rivrs.geysermodelenginepackgenerator.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public record BlueprintSettings(@SerializedName("head_rotation") boolean headRotation, String material,
                                @SerializedName("blend_transition") boolean blendTransition,
                                @SerializedName("per_texture_uv_size") PerTextureUvSize perTextureUvSize,
                                @SerializedName("binding_bones") BindingBones bindingBones
) {

    public record PerTextureUvSize(List<Integer> texture) {
    }

    public record BindingBones(List<String> textures) {
    }
}
