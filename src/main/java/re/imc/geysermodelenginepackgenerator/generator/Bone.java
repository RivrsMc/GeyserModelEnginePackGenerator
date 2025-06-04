package re.imc.geysermodelenginepackgenerator.generator;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Bone {

    private String name;
    private String parent;
    private Set<Bone> children = new HashSet<>();
    private Set<Bone> allChildren = new HashSet<>();
}
