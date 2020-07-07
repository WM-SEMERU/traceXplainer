package io.jenkins.plugins.traceability.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import io.jenkins.plugins.traceability.model.types.LikertScale;

//@XStreamAlias("link")
public class TraceabilityLink implements Comparable<TraceabilityLink> {

    private Artifact source;
    private Artifact target;
    private double similarity;
    private double feedback = -1;
    private double trace = -1;
    private LikertScale confidence;

    public TraceabilityLink(String source) {
        this.source = new Artifact(source);
    }

    public TraceabilityLink(String source, String target) {
        this.source = new Artifact(source);
        this.target = new Artifact(target);
        this.similarity = -1;
    }

    public TraceabilityLink(Artifact source, Artifact target, double similarity) {
        this.source = source;
        this.target = target;
        this.similarity = similarity;
    }

    public TraceabilityLink(String source, String target, double similarity) {
        this.source = new Artifact(source);
        this.target = new Artifact(target);
        this.similarity = similarity;
    }

    @Override
    public String toString() {
        return "TraceabilityLink{" +
                "source=" + source +
                ", target=" + target +
                ", similarity=" + similarity +
                ", feedback=" + feedback +
                ", trace=" + trace +
                ", confidence=" + confidence +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TraceabilityLink) {
            TraceabilityLink link = (TraceabilityLink) obj;
            return link.getSource().equals(this.getSource()) && link.getTarget().equals(this.getTarget());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getSource().hashCode() + getTarget().hashCode();
    }

    public Artifact getSource() {
        return source;
    }

    public void setSource(Artifact source) {
        this.source = source;
    }

    public Artifact getTarget() {
        return target;
    }

    public void setTarget(Artifact target) {
        this.target = target;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public double getFeedback() {
        return feedback;
    }

    public void setFeedback(double feedback) {
        this.feedback = feedback;
    }

    public double getTrace() {
        return trace;
    }

    public void setTrace(double trace) {
        this.trace = trace;
    }

    public LikertScale getConfidence() {
        return confidence;
    }

    public void setConfidence(LikertScale confidence) {
        this.confidence = confidence;
    }

    public double getFinalValue(){
        return getFeedback() == -1 ? getSimilarity() : getFeedback();
    }
    @Override
    public int compareTo(TraceabilityLink o) {
        double valueThis = getFeedback() == -1 ? getSimilarity() : getFeedback();
        double valueObject = o.getFeedback() == -1 ? o.getSimilarity() : o.getFeedback();
        return Double.compare(valueThis, valueObject);
    }
}
