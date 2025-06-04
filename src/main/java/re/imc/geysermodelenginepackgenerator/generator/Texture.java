package re.imc.geysermodelenginepackgenerator.generator;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Texture {

    private String modelId;
    private String path;
    private Set<String> bindingBones;
    private byte[] image;

}
