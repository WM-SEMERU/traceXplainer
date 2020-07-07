package io.jenkins.plugins.traceability.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class Artifact {

    @XStreamAlias("id")
    private String nameId;
    private String data;
    private String path;

    public Artifact(String nameId) {
        this.nameId = nameId;
    }

    public Artifact(String nameId, String path) {
        this.nameId = nameId;
        this.path = path;
    }

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return nameId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Artifact) {
            Artifact artifact = (Artifact) obj;
            if (this.getNameId() == null) {
                if (artifact.getNameId() == null) {
                    return true;
                }
            } else {
                if (artifact.getNameId() != null) {
                    return artifact.getNameId().equals(this.getNameId());
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + getNameId().hashCode();
        return result;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
