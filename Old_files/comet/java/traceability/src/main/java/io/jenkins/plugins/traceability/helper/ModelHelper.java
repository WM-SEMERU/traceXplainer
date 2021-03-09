package io.jenkins.plugins.traceability.helper;

import io.jenkins.plugins.traceability.model.Artifact;
import io.jenkins.plugins.traceability.model.AssociationModel;
import io.jenkins.plugins.traceability.model.IRModel;
import io.jenkins.plugins.traceability.model.TraceabilityLink;

import java.io.*;
import java.util.*;

public class ModelHelper {

    public static Map.Entry<String, IRModel> loadIRModel(File file) {
        return loadIRModel(file, null, null);
    }

    public static Map.Entry<String, IRModel> loadIRModel(File file, HashMap<String, String> sourceCodeList, File workspace) {
        IRModel model = null;
        String name = null;
        String parameters = null;
        String corpus = null;
        String thresholdTechnique = null;
        double threshold = 0;
        Artifact source;
        Artifact target;
        double value;
        String[] split;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String st;
            while ((st = br.readLine()) != null) {
                // Metadata info
                if (st.startsWith("#")) {
                    if (st.contains(" name:")) {
                        name = st.split(": ")[1];
                    } else if (st.contains("corpus_name:")) {
                        corpus = st.split(": ")[1];
                    } else if (st.contains("threshold_technique:")) {
                        thresholdTechnique = st.split(": ")[1];
                    } else if (st.contains("parameters:")) {
                        parameters = st.split("parameters: ")[1];
                    } else if (st.contains("threshold:")) {
                        try {
                            threshold = Double.parseDouble(st.split(": ")[1]);
                        } catch (NumberFormatException e) {
                            threshold = 0;
                        }
                    }
                } else if (!st.isEmpty()) {
                    if (model == null) {
                        model = new IRModel(name, thresholdTechnique, threshold, file.getName(), parameters);
                    }
                    // Similarity or probability values
                    split = st.split(" ");
                    // Fixing path and getting relative path in the workspace
                    source = new Artifact(split[0]);
                    if (sourceCodeList != null && workspace != null) {
                        String fullPath = sourceCodeList.get(split[1]);
                        fullPath = fullPath.substring(workspace.getAbsolutePath().length());
                        target = new Artifact(split[1], fullPath);
                    } else {
                        target = new Artifact(split[1]);
                    }

                    value = Double.parseDouble(split[2]);
                    model.addTraceabilityLink(new TraceabilityLink(source, target, value));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new AbstractMap.SimpleEntry(corpus, model);
    }

    public static AssociationModel loadAssociationModel(String name, File irModels, HashMap<String, String> sourceCodeList, File workspace) {
        AssociationModel asm = new AssociationModel(name);
        ArrayList<IRModel> models = new ArrayList<>();
        File[] irms = irModels.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".tm");
            }
        });
        for (File irm : irms) {
            Map.Entry<String, IRModel> modelEntry = loadIRModel(irm, sourceCodeList, workspace);
            System.out.println(modelEntry);
            models.add(modelEntry.getValue());
            // All of them should be the same
            asm.setCorpus(modelEntry.getKey());
        }
        asm.setReady(true);
        asm.setModels(models);
        return asm;
    }

    public static AssociationModel sampleModels(int requirements, AssociationModel asm, File output) {
        AssociationModel tempAsm = new AssociationModel("ASM sampling");
        List<TraceabilityLink> linksSampling = asm.getModels().iterator().next().getLinks().subList(0, requirements);

        for (IRModel model : asm.getModels()) {
            IRModel tempModel = new IRModel(model.getName(), model.getThresholdTechnique(), model.getThreshold(), model.getFileName().replace(".tm", "-sampling.tm"), model.getParameters());
            for (TraceabilityLink link : linksSampling) {
                int index = model.getLinks().indexOf(link);
                if (index != -1) {
                    // Add it to the new model
                    tempModel.addTraceabilityLink(model.getLinks().get(index));
                }
            }
            tempAsm.getModels().add(tempModel);
            writeModel2File(asm.getCorpus(), tempModel, output);
        }

        return tempAsm;
    }

    public static void writeModel2File(String corpus, IRModel model, File rootFolder) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(rootFolder.getAbsolutePath() + File.separator + model.getFileName());
            PrintWriter printWriter = new PrintWriter(fileWriter);
            //# Trace Model
            //# name: Inference Variational
            //# parameters: {'similarity_metric': 'hellinger', 'n_topics': 5}
            //# corpus_name: LibEST (0_1)
            //# threshold_technique: n/a
            //# threshold: n/a
            printWriter.println("# Trace Model");
            printWriter.println("# name: " + model.getName());
            printWriter.println("# parameters: " + model.getParameters());
            printWriter.println("# corpus_name: " + corpus);
            printWriter.println("# threshold_technique: " + model.getThresholdTechnique());
            printWriter.println("# threshold: " + (model.getThreshold() == 0 ? "n/a" : model.getThreshold()));
            for (TraceabilityLink link : model.getLinks()) {
                printWriter.println(link.getSource() + " " + link.getTarget() + " " + link.getSimilarity());
            }

            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TraceabilityLink findLink(HashMap<Artifact, Set<TraceabilityLink>> map, TraceabilityLink link) {
        return findLink(map, link.getSource(), link.getTarget());
    }

    public static TraceabilityLink findLink(HashMap<Artifact, Set<TraceabilityLink>> map, Artifact source, Artifact target) {
        if (map != null) {
            Set<TraceabilityLink> links = map.get(source);
            for (TraceabilityLink link : links) {
                if (link.getTarget().equals(target)) {
                    return link;
                }
            }
        }
        // If the link hasn't been preprocessed we won't have information about it on the file
        return null;
    }
}
