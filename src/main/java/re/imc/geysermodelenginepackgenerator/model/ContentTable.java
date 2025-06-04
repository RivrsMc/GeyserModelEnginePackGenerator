package re.imc.geysermodelenginepackgenerator.model;

import java.util.List;

public record ContentTable(List<Entry> content) {

    public void add(String path) {
        this.content.add(new Entry(path));
    }

    public record Entry(String path) {

    }
}
