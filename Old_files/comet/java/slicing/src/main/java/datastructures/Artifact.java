package datastructures;

/**
 * Created by david on 9/16/17.
 */
public class Artifact {
    private ArtifactType type;
    private String name;

    public Artifact(ArtifactType type, String name) {
        this.type = type;
        this.name = name;
    }

    public ArtifactType getType() {
        return type;
    }

    public void setType(ArtifactType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
