package io.jenkins.plugins.traceability.model;

import java.util.*;

public class IRModel {

    private static final String NOT_LINKED = "1";
    private static final String UNSURE = "2";
    private static final String LINKED = "3";
    private String name;
    private String thresholdTechnique;
    private double threshold;
    private String fileName;
    private String parameters;
    private HashMap<Artifact, Set<TraceabilityLink>> traceabilityMatrix = new HashMap<>();
    private List<TraceabilityLink> links = new ArrayList<>();

    public IRModel(String name, String thresholdTechnique, double threshold, String fileName, String parameters, List<TraceabilityLink> links) {
        this.name = name;
        this.thresholdTechnique = thresholdTechnique;
        this.threshold = threshold;
        this.fileName = fileName;
        this.parameters = parameters;
        addTraceabilityLinks(links);
    }

    public IRModel(String name, String thresholdTechnique, double threshold, String fileName, String parameters) {
        this.name = name;
        this.thresholdTechnique = thresholdTechnique;
        this.threshold = threshold;
        this.fileName = fileName;
        this.parameters = parameters;
    }

    public IRModel() {
    }

    public void addTraceabilityLinks(List<TraceabilityLink> links) {
        for (TraceabilityLink link : links) {
            addTraceabilityLink(link);
        }
    }

    public void addTraceabilityLink(TraceabilityLink link) {
        Artifact requirement = link.getSource();
        // Requirement is already there
        if (traceabilityMatrix.containsKey(requirement)) {
            Set<TraceabilityLink> list = traceabilityMatrix.get(requirement);
            list.add(link);
        } else {
            traceabilityMatrix.put(requirement, new HashSet<TraceabilityLink>(Arrays.asList(link)));
        }
        links.add(link);
    }

    public HashMap<Artifact, Set<TraceabilityLink>> getTraceabilityMatrix() {
        return traceabilityMatrix;
    }

    public void setTraceabilityMatrix(HashMap<Artifact, Set<TraceabilityLink>> traceabilityMatrix) {
        this.traceabilityMatrix = traceabilityMatrix;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThresholdTechnique() {
        return thresholdTechnique;
    }

    public void setThresholdTechnique(String thresholdTechnique) {
        this.thresholdTechnique = thresholdTechnique;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public List<TraceabilityLink> getLinks() {
        return links;
    }

    public void setLinks(List<TraceabilityLink> links) {
        this.links = links;
    }

    public HashMap<Artifact, Set<TraceabilityLink>> getTraceabilityMatrix(String filter) {
        HashMap<Artifact, Set<TraceabilityLink>> clone = new HashMap<>();
        Set<TraceabilityLink> values = null;
        // Iterate the original map with links
        for (Map.Entry<Artifact, Set<TraceabilityLink>> entry : traceabilityMatrix.entrySet()) {
            values = new HashSet<>();
            for (TraceabilityLink link : entry.getValue()) {
                // Include link if applies
                if (isFilterApplied(filter, link)) {
                    values.add(link);
                }
            }
            if (!values.isEmpty()) {
                clone.put(entry.getKey(), values);
            }
        }

        return clone;
    }

    private boolean isFilterApplied(String filter, TraceabilityLink link) {
        // filter is 1 then [Not Linked]
        return link.getFinalValue() >= 0 && link.getFinalValue() < 0.4 && filter.equals(NOT_LINKED) ||
                // filter is 2 then [Unsure]
                link.getFinalValue() >= 0.4 && link.getFinalValue() < 0.7 && filter.equals(UNSURE) ||
                // filter is 3 then [Linked]
                link.getFinalValue() >= 0.7 && link.getFinalValue() < 1 && filter.equals(LINKED);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Artifact getSourceArtifact(String nameId) {
        for (Artifact artifact : getTraceabilityMatrix().keySet()) {
            if(artifact.getNameId().equals(nameId)){
                return artifact;
            }
        }
        return new Artifact("");
    }
}
