package correlation;

import datastructures.Artifact;
import datastructures.ArtifactType;
import datastructures.Tuple;

import java.io.*;
import java.util.*;

/**
 * Created by david on 9/16/17.
 */
public class ExtractTracLinks {

    private HashMap<String, HashMap<Artifact, Double>> requirementGraph;
    private HashMap<String, Set<Artifact>> testCasesTracesGraph;
    private HashMap<String, HashMap<String, List<Double>>> inferredGraph;

    private String userPath = System.getProperty("user.dir");

    public ExtractTracLinks(double threshold) {
        this.requirementGraph = extractRequirementsLinks(threshold);
        this.testCasesTracesGraph = extractTestCases2SCLinks();
        this.inferredGraph = inferredLinks(threshold);
    }

    public HashMap<String, HashMap<Artifact, Double>> getRequirementGraph() {
        return requirementGraph;
    }

    public HashMap<String, Set<Artifact>> getTestCasesTracesGraph() {
        return testCasesTracesGraph;
    }

    public HashMap<String, HashMap<String, List<Double>>> getInferredGraph() {
        return inferredGraph;
    }

    /**
     * Graph Extraction Requirements with a defined T threshold
     *
     * @return
     */
    private HashMap<String, HashMap<Artifact, Double>> extractRequirementsLinks(double threshold) {
        String cFileName = userPath + "/files/correlationsR2C.txt";
        HashMap<String, HashMap<Artifact, Double>> requirements = new HashMap<>();
        //Calling to process R2C
        readRequirementsLinks(ArtifactType.SOURCECODE, cFileName, requirements, threshold);
        //Calling to process R2T
        cFileName = userPath + "/files/correlationsR2T.txt";
        readRequirementsLinks(ArtifactType.TESTCASE, cFileName, requirements, threshold);
        return requirements;

    }

    /**
     * Graph of Test Cases to Source Code
     *
     * @return
     */
    private HashMap<String, Set<Artifact>> extractTestCases2SCLinks() {
        String cFileName = userPath + "/files/correlationsT2C.txt";
        HashMap<String, Set<Artifact>> traces = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(cFileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitStr = line.trim().split("(\\s|,|=>)");
                String key = null;
                for (String k : traces.keySet()) {
                    if (k.equals(splitStr[0])) {
                        key = k;
                        traces.get(key).add(new Artifact(ArtifactType.SOURCECODE, splitStr[1]));
                        break;
                    }
                }

                if (key == null) {
                    Set<Artifact> newArtifact = new HashSet<>();
                    newArtifact.add(new Artifact(ArtifactType.SOURCECODE, splitStr[1]));
                    traces.put(splitStr[0], newArtifact);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return traces;
    }

    private static void readRequirementsLinks(ArtifactType childType, String cFileName,
                                              HashMap<String, HashMap<Artifact, Double>> requirements,
                                              double threshold) {

        try (BufferedReader br = new BufferedReader(new FileReader(cFileName))) {
            String line;
            double weight;
            String key;
            while ((line = br.readLine()) != null) {
                String[] splitStr = line.trim().split("(\\s|,|=>)");
                key = null;
                weight = Double.parseDouble(splitStr[2]);

                //Threshold Verification
                if (weight >= threshold) {
                    for (String k : requirements.keySet()) {
                        if (k.equals(splitStr[0])) {
                            key = k;
                            requirements.get(key).put(new Artifact(childType, splitStr[1]), weight);
                            break;
                        }
                    }

                    if (key == null) {
                        HashMap<Artifact, Double> requirementChild = new HashMap<>();
                        requirementChild.put(new Artifact(childType, splitStr[1]), weight);
                        requirements.put(splitStr[0],
                                requirementChild);
                    }
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private HashMap<String, HashMap<String, List<Double>>> inferredLinks(double threshold) {
        HashMap<String, HashMap<String, List<Double>>> possibleLinks = new HashMap<>();

        //Top Down Approach Requirement Graphs
        for (String req : requirementGraph.keySet()) {
            for (Artifact target : requirementGraph.get(req).keySet()) {
                if (target.getType() == ArtifactType.TESTCASE) {
                    //R->T
                    insertLink(req, target.getName(), requirementGraph.get(req).get(target), possibleLinks);

                } else {
                    if (target.getType() == ArtifactType.SOURCECODE) {
                        //R->C
                        insertLink(req, target.getName(), requirementGraph.get(req).get(target), possibleLinks);
                    }
                }

            }
        }

        //Inserting Code Traces T->C
        for (String source : testCasesTracesGraph.keySet()) {
            for (Artifact target : testCasesTracesGraph.get(source)) {
                insertLink(source, target.getName(), 1.0, possibleLinks);
            }
        }

        //Bottom Up Approach
        for (String req : requirementGraph.keySet()) {

            ArrayList<String> sourceCode = new ArrayList<>();
            ArrayList<String> testCases = new ArrayList<>();

            for (Artifact target : requirementGraph.get(req).keySet()) {
                if (target.getType().equals(ArtifactType.SOURCECODE)) {
                    sourceCode.add(target.getName());
                } else {
                    if (target.getType().equals(ArtifactType.TESTCASE)) {
                        testCases.add(target.getName());
                    }
                }
            }

            //Case 1: Infer T->C
            for (String test : testCases) {
                if (possibleLinks.containsKey(test)) {
                    for (String src : sourceCode) {
                        if (!possibleLinks.get(test).containsKey(src)) {
                            //T -> C Inferred from traces
                            insertLink(test, src, threshold, possibleLinks);
                        }
                    }
                } else {
                    for (String src : sourceCode) {
                        //T -> C Inferred from IR techniques
                        insertLink(test, src, threshold, possibleLinks);
                    }
                }
            }

            //Case 2: Infer R->C
            for (String test : testCases) {
                if (possibleLinks.containsKey(test)) {
                    for (String pssArt : possibleLinks.get(test).keySet()) {
                        if (!sourceCode.contains(pssArt)) {
                            //R -> C Inferred by means of C
                            insertLink(req, pssArt, threshold, possibleLinks);
                            sourceCode.add(pssArt);
                        }
                    }
                }else{
                    System.out.println("Something is wrong with this testcase: " + test);
                }
            }

            //Case 3: Infer R->T
            for (String src : sourceCode) {
                for (String pssArt : possibleLinks.keySet()) {
                    if (!pssArt.equals(req) && !requirementGraph.containsKey(pssArt) &&
                            possibleLinks.get(pssArt).containsKey(src) &&
                            !testCases.contains(pssArt)) {
                        //R -> T Inferred by means of C
                        insertLink(req, pssArt, threshold, possibleLinks);
                        testCases.add(pssArt);
                    }
                }
            }
        }
        return possibleLinks;
    }

    private void insertLink(String source, String target, double weight,
                            HashMap<String, HashMap<String, List<Double>>> hashLinks) {

        if (hashLinks.containsKey(source)) {
            if (hashLinks.get(source).containsKey(target)) {
                hashLinks.get(source).get(target).add(weight);
            } else {
                hashLinks.get(source).put(target, new ArrayList<>());
                hashLinks.get(source).get(target).add(weight);
            }
        } else {
            hashLinks.put(source, new HashMap<>());
            hashLinks.get(source).put(target, new ArrayList<>());
            hashLinks.get(source).get(target).add(weight);
        }

    }

    public void writeGraphi(HashMap<String, HashMap<String, List<Double>>> graph) {
        try {
            PrintWriter writer = new PrintWriter(userPath
                    + "/files/outInferredGraph.txt", "UTF-8");

            double meanWeight = 0;

            for (String key : graph.keySet()) {
                for (String valueKey : graph.get(key).keySet()) {
                    for(double w: graph.get(key).get(valueKey)){
                        meanWeight += w;
                    }
                    meanWeight = meanWeight /graph.get(key).get(valueKey).size();
                    writer.println(key + " " + valueKey + " " + meanWeight);
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void increaseSimilaritybyMerging(){
        //Read Similarities between Requirements and Source Code
        String cFileName = userPath + "/files/correlationsR2C.txt";
        String cFilecombined = "LDA01InferredJS";

        writeSimilarities(readSimilarities(cFileName), cFilecombined + "01");

        //Read Similarities between Requirements and TestCases
        cFileName = userPath + "/files/correlationsR2T.txt";
        writeSimilarities(readSimilarities(cFileName), cFilecombined + "02");
    }

    private void writeSimilarities(ArrayList<Tuple<String, Tuple<String, Double>>> register, String cFilename) {
        try {
            PrintWriter writer = new PrintWriter(userPath
                    + "/files/"+ cFilename +".txt", "UTF-8");

            for (Tuple<String, Tuple<String, Double>> key : register) {
                writer.println(key.x + " " + key.y.x + " " + key.y.y);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Tuple<String, Tuple<String, Double>>> readSimilarities(String cFileName){
        ArrayList<Tuple<String, Tuple<String, Double>>> register = null;
        try (BufferedReader br = new BufferedReader(new FileReader(cFileName))) {
            String line;
            double weight = 0;
            String requirement, artifact;
            register = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] splitStr = line.trim().split("(\\s|,|=>)");

                requirement = splitStr[0];
                artifact = splitStr[1];
                weight = Double.parseDouble(splitStr[2]);

                //Verify the weight with the inferred structure
                if( inferredGraph.containsKey(requirement)){
                    if(inferredGraph.get(requirement).containsKey(artifact)){
                        weight = 0;
                        for(double w: inferredGraph.get(requirement).get(artifact)){
                            weight += w;
                        }
                        weight = weight /inferredGraph.get(requirement).get(artifact).size();

                    }
                }

                register.add(new Tuple<>(requirement, new Tuple<>(artifact,weight)));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return register;
    }

    /**
     * Simple Unit Test
     *
     * @param args
     */
    public static void main(String[] args) {
        ExtractTracLinks extractLink = new ExtractTracLinks(0.1);
        //extractLink.writeGraph(extractLink.getRequirementGraph());
        extractLink.writeGraphi(extractLink.inferredGraph);
        extractLink.increaseSimilaritybyMerging();
    }

}
