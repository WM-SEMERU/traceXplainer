package io.jenkins.plugins.traceability.model;

import java.util.ArrayList;
import java.util.List;

public class AssociationModel {

    private String name;
    private String corpus;
    private boolean ready;
    private List<IRModel> models = new ArrayList<>();

    public AssociationModel(String name) {
        this.name = name;
    }

    public AssociationModel(String name, boolean ready) {
        this.name = name;
        this.ready = ready;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public List<IRModel> getModels() {
        return models;
    }

    public void setModels(List<IRModel> models) {
        this.models = models;
    }

    public String getCorpus() {
        return corpus;
    }

    public void setCorpus(String corpus) {
        this.corpus = corpus;
    }
}
