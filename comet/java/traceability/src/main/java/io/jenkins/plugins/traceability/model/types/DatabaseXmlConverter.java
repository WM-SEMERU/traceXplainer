package io.jenkins.plugins.traceability.model.types;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import io.jenkins.plugins.traceability.model.Artifact;
import io.jenkins.plugins.traceability.model.DatabaseXml;
import io.jenkins.plugins.traceability.model.TraceabilityLink;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseXmlConverter implements Converter {
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        DatabaseXml db = (DatabaseXml) source;
        writer.startNode("links");
        for (TraceabilityLink link : db.getLinks()) {
//            private Artifact source;
//            private Artifact target;
//            private double similarity;
//            private double feedback = -1;
//            private double trace = -1;
//            private LikertScale confidence;
            writer.startNode("link");

            writer.startNode("source");
            writer.startNode("nameId");
            writer.setValue(link.getSource().getNameId());
            writer.endNode();
            writer.endNode();

            writer.startNode("target");
            writer.startNode("nameId");
            writer.setValue(link.getTarget().getNameId());
            writer.endNode();
            writer.startNode("path");
            writer.setValue(link.getTarget().getPath());
            writer.endNode();
            writer.endNode();

            writer.startNode("similarity");
            writer.setValue(link.getSimilarity() + "");
            writer.endNode();
            writer.startNode("feedback");
            writer.setValue(link.getFeedback() + "");
            writer.endNode();
            writer.startNode("trace");
            writer.setValue(link.getTrace() + "");
            writer.endNode();
            writer.startNode("confidence");
            writer.setValue(link.getConfidence().toString());
            writer.endNode();

            writer.endNode();
        }
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        DatabaseXml db = new DatabaseXml();
        reader.moveDown(); //links
        List<TraceabilityLink> list = new ArrayList<>();
        while (reader.hasMoreChildren()) {
            reader.moveDown(); //link

            reader.moveDown(); //source
            reader.moveDown(); //nameId
            String nameId = reader.getValue();
            Artifact source = new Artifact(nameId);
            reader.moveUp();
            reader.moveUp();

            reader.moveDown(); //target
            reader.moveDown(); //nameId
            nameId = reader.getValue();
            Artifact target = new Artifact(nameId);
            reader.moveUp();
            reader.moveDown(); //path
            String path = reader.getValue();
            target.setPath(path);
            reader.moveUp();
            reader.moveUp();

            reader.moveDown(); //similarity
            double similarity = Double.parseDouble(reader.getValue());
            reader.moveUp();
            reader.moveDown(); //feedback
            double feedback = Double.parseDouble(reader.getValue());
            reader.moveUp();
            reader.moveDown(); //trace
            double trace = Double.parseDouble(reader.getValue());
            reader.moveUp();
            reader.moveDown(); //confidence
            String confidence = reader.getValue();
            reader.moveUp();

            TraceabilityLink link = new TraceabilityLink(source,target,similarity);
            link.setFeedback(feedback);
            link.setTrace(trace);
            link.setConfidence(LikertScale.valueOf(confidence));
            reader.moveUp();
            list.add(link);
        }

        reader.moveUp();

        db.setLinks(new HashSet<TraceabilityLink>(list));
        return db;
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(DatabaseXml.class);
    }
}
