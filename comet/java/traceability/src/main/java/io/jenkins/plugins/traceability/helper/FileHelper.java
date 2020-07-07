package io.jenkins.plugins.traceability.helper;

import com.thoughtworks.xstream.XStream;
import io.jenkins.plugins.traceability.BuildAnalysisAction;
import io.jenkins.plugins.traceability.model.Artifact;
import io.jenkins.plugins.traceability.model.DatabaseXml;
import io.jenkins.plugins.traceability.model.IRModel;
import io.jenkins.plugins.traceability.model.TraceabilityLink;
import io.jenkins.plugins.traceability.model.types.DatabaseXmlConverter;
import io.jenkins.plugins.traceability.model.types.LikertScale;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;

public class FileHelper {


    public static String readFile(String file) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String st;
            while ((st = br.readLine()) != null) {
                // Metadata info
                builder.append(st + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static boolean writeFile(String file, String content) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);

            writer.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void addTargetFile2Artifacts(IRModel vi, String sourceCodePath, File workspace) {
        if (vi != null) {
            HashMap<String, String> sourceCodeList = new HashMap<>();
            findAllFiles(new File(sourceCodePath), sourceCodeList);
            Set<TraceabilityLink> artifactsResult = vi.getTraceabilityMatrix().values().iterator().next();
            for (TraceabilityLink link : artifactsResult) {
                String nameId = link.getTarget().getNameId();
                // Getting relative path in the workspace
                String fullPath = sourceCodeList.get(nameId);
                fullPath = fullPath.substring(workspace.getAbsolutePath().length());
                link.getTarget().setPath(sourceCodeList.get(fullPath));
            }
        }
    }

    public static void findAllFiles(File dir, HashMap<String, String> src) {
        findAllFiles(dir, src, null);
    }

    public static void findAllFiles(File dir, HashMap<String, String> src, String filter) {
        if (dir == null || dir.listFiles() == null) {
            return;
        }
        for (File entry : dir.listFiles()) {
            if (entry.isFile() && !entry.isHidden() && (filter == null || entry.getName().endsWith(filter))) {
                src.put(entry.getName(), entry.getAbsolutePath());
            } else if (entry.isDirectory()) {
                findAllFiles(entry, src, filter);
            }
        }
    }

    public static String saveLink(TraceabilityLink link, File db, BuildAnalysisAction buildAnalysisAction) {
        // Validate db exists
        if (!db.getAbsoluteFile().exists()) {
            try {
                db.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Add element to db list and overwrite the file
        DatabaseXml linksDb = loadLinks(db.getAbsoluteFile(), buildAnalysisAction);
        linksDb.addLink(link);

        XStream xStream = new XStream();
        xStream.registerConverter(new DatabaseXmlConverter());
        //xStream.autodetectAnnotations(true);
        String result = xStream.toXML(linksDb);
        writeFile(db.getAbsolutePath(), result);

        return result;
    }

    public static DatabaseXml loadLinks(File db, BuildAnalysisAction buildAnalysisAction) {
        // Validate existence of db
        if (!db.getAbsoluteFile().exists()) {
            return new DatabaseXml();
        }
        // Read internal db
//        String content = readFile(db.getAbsolutePath());

//        buildAnalysisAction.addLog(FileHelper.class, content);
        // Transform database from xml format to object using security flags
//        Class<?>[] classes = new Class[]{TraceabilityLink.class, Set.class, HashSet.class, Artifact.class, String.class};
        XStream xStream = new XStream();
        //XStream.setupDefaultSecurity(xStream);
//        xStream.allowTypes(classes);
//        xStream.alias("link", TraceabilityLink.class);
        xStream.registerConverter(new DatabaseXmlConverter());
//        xStream.aliasField("id", Artifact.class, "nameId");

//        xStream.autodetectAnnotations(true);
        try {

            DatabaseXml databaseXml = readDatabase(db, buildAnalysisAction);
            buildAnalysisAction.addLog(FileHelper.class, databaseXml.toString());
//            DatabaseXml database = (DatabaseXml) xStream.fromXML(content);
            return databaseXml;
        } catch (com.thoughtworks.xstream.io.StreamException e) {
            return new DatabaseXml();
        } catch (Exception e) {
            buildAnalysisAction.addLog(FileHelper.class, e.getLocalizedMessage());
            buildAnalysisAction.addLog(FileHelper.class, e.getClass().toString());
//            buildAnalysisAction.addLog(FileHelper.class, e.getCause().toString());
            buildAnalysisAction.addLog(FileHelper.class, e.getMessage());
            buildAnalysisAction.addLog(FileHelper.class, Arrays.toString(e.getStackTrace()));

            return new DatabaseXml();
        }
    }

    public static DatabaseXml readDatabase(File db, BuildAnalysisAction ba) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DatabaseXml database = new DatabaseXml();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            // create a new document from input source
            FileInputStream fis = new FileInputStream(db);
            InputSource is = new InputSource(fis);
            Document doc = builder.parse(is);

            // get the first element
            Element element = doc.getDocumentElement();

//            ba.addLog(FileHelper.class, "(1). "+element.getTextContent());

            // get all child databaseXml
            NodeList databaseXml = element.getChildNodes();
            Node links = databaseXml.item(1);

//            ba.addLog(FileHelper.class, "(2). "+links.getTextContent());
//            ba.addLog(FileHelper.class, "(2.1). "+databaseXml.item(1).getTextContent());
//            ba.addLog(FileHelper.class, "(2.1). "+databaseXml.item(2).getTextContent());

            // print the text content of each child
            for (int i = 0; i < links.getChildNodes().getLength(); i++) {
                Node link = links.getChildNodes().item(i);

                if (link.getNodeName().equals("link")) {
                    Artifact source = null;
                    Artifact target = null;
                    double similarity = 0;
                    double feedback = 0;
                    double trace = 0;
                    String confidence = "";

                    for (int j = 0; j < link.getChildNodes().getLength(); j++) {
                        Node item = link.getChildNodes().item(j);
//                        System.out.println(item);
                        if (item.getNodeName().equals("source")) {
                            source = new Artifact(item.getTextContent().trim());
                        } else if (item.getNodeName().equals("target")) {
                            for (int k = 0; k < item.getChildNodes().getLength(); k++) {
                                Node subitem = item.getChildNodes().item(k);
                                if (subitem.getNodeName().equals("nameId")) {
                                    target = new Artifact(subitem.getTextContent());
                                } else if (subitem.getNodeName().equals("path")) {
                                    target.setPath(subitem.getTextContent());
                                }
                            }
                        } else if (item.getNodeName().equals("similarity")) {
//                            System.out.println("::"+item.getNodeValue()+"::");
//                            System.out.println("::"+item.getTextContent()+"::");
                            similarity = Double.parseDouble(item.getTextContent());
                        } else if (item.getNodeName().equals("feedback")) {
                            feedback = Double.parseDouble(item.getTextContent());
                        } else if (item.getNodeName().equals("trace")) {
                            trace = Double.parseDouble(item.getTextContent());
                        } else if (item.getNodeName().equals("confidence")) {
                            confidence = item.getTextContent();
                        }

                    }

//                    System.out.println(source);
//                    System.out.println(target);
//                    System.out.println(similarity);
//                    System.out.println(feedback);
//                    System.out.println(trace);
//                    System.out.println(confidence);
                    TraceabilityLink traceabilityLink = new TraceabilityLink(source, target, similarity);
                    traceabilityLink.setFeedback(feedback);
                    traceabilityLink.setTrace(trace);
                    traceabilityLink.setConfidence(LikertScale.valueOf(confidence));
                    database.addLink(traceabilityLink);
                }


            }
        } catch (org.xml.sax.SAXParseException e) {
            return database;
        } catch (Exception e) {
//            ba.addLog(FileHelper.class, e.getLocalizedMessage());
//            ba.addLog(FileHelper.class, e.getClass().toString());
////            buildAnalysisAction.addLog(FileHelper.class, e.getCause().toString());
//            ba.addLog(FileHelper.class, e.getMessage());
//            ba.addLog(FileHelper.class, Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
        return database;
    }

    public static HashMap<Artifact, List<Artifact>> loadTraces(String traces, Set<TraceabilityLink> fullSrc, Set<TraceabilityLink> fullTests) {
        String file = readFile(traces);
        String[] lineSplit = null;
        HashMap<Artifact, List<Artifact>> map = new HashMap<>();

        String[] split = file.split("\n");
        for (String line : split) {
            lineSplit = line.split(" ");
            // Test case
            Artifact testCase = new Artifact(lineSplit[0]);
            // Src
            Artifact sourceCode = new Artifact(lineSplit[1]);

            // TODO: make this more efficient
            for (TraceabilityLink linkSrc : fullSrc) {
                if (linkSrc.getTarget().equals(sourceCode)) {
                    sourceCode = linkSrc.getTarget();
                    break;
                }
            }
            for (TraceabilityLink linkTest : fullTests) {
                if (linkTest.getTarget().equals(testCase)) {
                    testCase = linkTest.getTarget();
                    break;
                }
            }

            if (!map.containsKey(testCase)) {
                map.put(testCase, new ArrayList<Artifact>());
            }
            map.get(testCase).add(sourceCode);
        }
        return map;
    }
}
