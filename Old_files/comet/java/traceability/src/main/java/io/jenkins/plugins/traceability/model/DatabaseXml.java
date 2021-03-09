package io.jenkins.plugins.traceability.model;

import java.util.HashSet;
import java.util.Set;

public class DatabaseXml {

    Set<TraceabilityLink> links = new HashSet<>();

    public DatabaseXml() {
    }

    public DatabaseXml(Set<TraceabilityLink> links) {

        this.links = links;
    }

    public void addLink(TraceabilityLink link) {
        if (links.contains(link)) {
            links.remove(link);
        }
        links.add(link);
    }

    public Set<TraceabilityLink> getLinks() {
        return links;
    }

    public void setLinks(Set<TraceabilityLink> links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return "DatabaseXml{" +
                "links=" + links +
                '}';
    }
}
