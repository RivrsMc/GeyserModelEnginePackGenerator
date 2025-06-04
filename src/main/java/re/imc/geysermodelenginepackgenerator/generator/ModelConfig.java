package re.imc.geysermodelenginepackgenerator.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ModelConfig {

    @SerializedName("head_rotation")
    private boolean enableHeadRotation = true;
    @SerializedName("material")
    private String material = "entity_alphatest_change_color_one_sided";
    @SerializedName("blend_transition")
    private boolean enableBlendTransition = true;
    @SerializedName("binding_bones")
    private Map<String, Set<String>> bingingBones = new HashMap<>();
    @SerializedName("anim_textures")
    private Map<String, AnimTextureOptions> animTextures = new HashMap<>();
    @SerializedName("texture_materials")
    private Map<String, String> textureMaterials = new HashMap<>();
    @SerializedName("per_texture_uv_size")
    private Map<String, Integer[]> perTextureUvSize;
    @SerializedName("disable_part_visibility")
    private boolean disablePartVisibility = true;

    public Map<String, String> getTextureMaterials() {
        return textureMaterials != null ? textureMaterials : Map.of();
    }

    public Map<String, Integer[]> getPerTextureUvSize() {
        return perTextureUvSize != null ? perTextureUvSize : Map.of();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class AnimTextureOptions {
        float fps;
        int frames;
    }
}
