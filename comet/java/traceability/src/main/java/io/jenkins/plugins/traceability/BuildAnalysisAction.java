package io.jenkins.plugins.traceability;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import io.jenkins.plugins.traceability.helper.FileHelper;
import io.jenkins.plugins.traceability.helper.HtmlHelper;
import io.jenkins.plugins.traceability.helper.ModelHelper;
import io.jenkins.plugins.traceability.helper.ThreadExecutor;
import io.jenkins.plugins.traceability.model.Artifact;
import io.jenkins.plugins.traceability.model.IRModel;
import io.jenkins.plugins.traceability.model.TraceabilityLink;
import io.jenkins.plugins.traceability.model.DatabaseXml;
import io.jenkins.plugins.traceability.model.types.LikertScale;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class BuildAnalysisAction implements Action {

    private static final NumberFormat FORMATTER = new DecimalFormat("#0.000");
    private HashMap<Artifact, List<Artifact>> test2src = new HashMap<>();
    private List<String> command;
    private List<String> consoleLog = new ArrayList<>();
    private List<String> modals = new ArrayList<>();
    private List<String> modalsTraces = new ArrayList<>();
    private List<String> modalsDeveloper = new ArrayList<>();
    private List<String> modalsTests = new ArrayList<>();

    private List<String> req2SrcNotLinkedReq = new ArrayList<>();
    private List<String> req2SrcNotLinkedSrc = new ArrayList<>();
    private List<String> req2TestNotLinkedReq = new ArrayList<>();
    private List<String> req2TestNotLinkedSrc = new ArrayList<>();


    private AbstractBuild<?, ?> build;
    private IRModel asm = null;
    private IRModel asm2 = null;
    private boolean finished = false;
    private String currentState = "";
    private String confidenceFilter = "3";
    private int linkFilter = 1;
    private static final String DB = "database.xml";

    public BuildAnalysisAction(List<String> command, AbstractBuild<?, ?> build) {
        this.command = command;
        this.build = build;
        File artifactsFolder = new File(build.getArtifactManager().root().toURI());
    }


    public IRModel getAsm() {
        return asm;
    }

    public void setAsm(IRModel asm) {
        this.asm = asm;
    }

    @CheckForNull
    @java.lang.Override
    public java.lang.String getIconFileName() {
        return "/plugin/traceability/img/cisco.png";
    }

    @CheckForNull
    @java.lang.Override
    public java.lang.String getDisplayName() {
        return "Traceability";
    }

    @CheckForNull
    @java.lang.Override
    public java.lang.String getUrlName() {
        return "traceability";
    }

    public void computeAnalysis() {
        ThreadExecutor thread = new ThreadExecutor(command, this);
        addLog(BuildAnalysisAction.class, "Thread started");
        thread.start();
    }

    public void addLog(Class clazz, String log) {
        consoleLog.add("[" + clazz.getSimpleName() + "] " + log);
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public boolean getFinished() {
        return finished;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

//    public Collection<String> getListFiles() {
//        return this.listFiles;
//    }

    public List<TraceabilityLink> getLinks() {
        if (asm != null) {
            return asm.getLinks();
        } else {
            return new ArrayList<>();
        }
    }

    public List<String> getConsoleLog() {
        return consoleLog;
    }

    public void setConsoleLog(List<String> consoleLog) {
        this.consoleLog = consoleLog;
    }

    public String visible() {
        return !finished ? "initial" : "none";
    }

    public String getTargetArtifacts(List<TraceabilityLink> targets) {
        String result = "";

        for (TraceabilityLink link : targets) {
            result += "[<a target=\"_blank\" href=\"/job/" + build.getProject().getName() + "/ws" + link.getTarget().getPath() + "\">" + link.getTarget().getNameId() + "</a> | " + FORMATTER.format(link.getSimilarity()) + "]<br> ";
        }

        return result;
    }

    public String getFinalProbabilityArtifact(TraceabilityLink link) {
        double expectedValue = link.getSimilarity();
        // If we have computed the feedback value show it
        if (link.getFeedback() != -1) {
            expectedValue = link.getFeedback();
        }
        String result = "[<a target=\"_blank\" href=\"/job/" + build.getProject().getName() + "/ws" + link.getTarget().getPath() + "\">" + link.getTarget().getNameId() + "</a> | " + FORMATTER.format(expectedValue) + "]";
        return result;
    }

    public String getLinkArtifact(Artifact artifact) {
        return "<a target=\"_blank\" href=\"/job/" + build.getProject().getName() + "/ws" + artifact.getPath() + "\">" + artifact.getNameId() + "</a>";
    }


    public boolean showTypeRelationship() {
        return linkFilter == 1 || linkFilter == 2;
    }

    public String getProbabilityArtifact(TraceabilityLink link) {
        String result = "<em>" + FORMATTER.format(link.getSimilarity()) + "</em>";
        return result;
    }

    public void doReloadLinkFilter(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        JSONObject json = req.getSubmittedForm();

        int linkFilter = json.getInt("linkFilter");
        this.linkFilter = linkFilter;
        rsp.sendRedirect(".");
    }

    public void doReloadFilter(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        JSONObject json = req.getSubmittedForm();

        String confidenceFilter = json.getString("confidenceFilter");
        this.confidenceFilter = confidenceFilter;
        rsp.sendRedirect(".");
    }

    public void doFeedback(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        JSONObject json = req.getSubmittedForm();

        String value = json.getString("confidenceDeveloper");
        if (value != null && !value.isEmpty()) {
            String[] split = value.split("::");
            String confidence = split[0];
            Artifact source = new Artifact(split[1]);
            Artifact target = new Artifact(split[2]);
            String typeModal = "";
            if (split.length > 3) {
                typeModal = split[3];
            }

//            for (TraceabilityLink link : asm.getTraceabilityMatrix().get(source)) {
//                if (link.getTarget().equals(target)) {
//                    feedbackLinks.put(link.getSource().getNameId() + link.getTarget().getNameId(), feedback);
//                    break;
//                }
//            }
//            addLog(BuildAnalysisAction.class, feedback + " :: " + asm.getTraceabilityMatrix().get(source) + " :: " + source + " :: " + target);

            /*
            String feedbackString = "";
            for (TraceabilityLink link : feedbackLinks) {
                feedbackString += link.getSource().getNameId() + "::" + link.getTarget().getNameId() + "::" + link.getSimilarity() + "#";
            }
            if (!feedbackString.isEmpty()) {
                feedbackString = feedbackString.substring(0, feedbackString.length() - 1);
            }

            String causalRootPath = new File(command.get(1)).getParentFile().getAbsolutePath();
            File artifactsFolder = new File(build.getArtifactManager().root().toURI());

            File[] irms = artifactsFolder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith("-sampling.tm");
                }
            });

            String files = "";
            for (File model : irms) {
                files += model.getName() + ",";
            }
            files = files.substring(0, files.length() - 1);

            List<String> commandAsm2 = Arrays.asList(command.get(0), causalRootPath + File.separator + "ComputeAsm2.py", feedbackString, files, artifactsFolder.getAbsolutePath() + File.separator);
            addLog(BuildAnalysisAction.class, "ASM2!!! :: " + commandAsm2.toString());

            CommandExecutor commandExecutor = new CommandExecutor(commandAsm2);
            try {
                commandExecutor.executeCommand();
                addLog(ThreadExecutor.class, commandExecutor.getStandardOutput().toString());
                addLog(ThreadExecutor.class, commandExecutor.getErrorOutput().toString());
                addLog(ThreadExecutor.class, "3. Association Model 2 executed");

                String asm2Result = artifactsFolder.getAbsolutePath() + File.separator + "NUTS.tm";
                Map.Entry<String, IRModel> viResult = ModelHelper.loadIRModel(new File(asm2Result));

            } catch (InterruptedException e) {
                e.printStackTrace();
                addLog(BuildAnalysisAction.class, "InterruptedException");
                addLog(BuildAnalysisAction.class, Arrays.toString(e.getStackTrace()));
            }
            */

            addLog(ThreadExecutor.class, "3. Association Model 2 executed");

            String extra = typeModal.isEmpty() ? "" : "-" + typeModal;
            File artifactsFolder = new File(build.getArtifactManager().root().toURI());
            String asm2Result = artifactsFolder.getAbsolutePath() + File.separator + "NUTS-" + confidence + extra + ".tm";
            // TODO: Check if the file exists
            Map.Entry<String, IRModel> nuts = ModelHelper.loadIRModel(new File(asm2Result));
//            addLog(ThreadExecutor.class, nuts.getValue().getTraceabilityMatrix().toString());

            boolean precomputed = false;
            if (nuts.getValue() != null) {
//                addLog(BuildAnalysisAction.class, "ASM2.0!!! :: " + source);
//                addLog(BuildAnalysisAction.class, "ASM2.0!!! :: " + target);
//                addLog(BuildAnalysisAction.class, "ASM2.0.1!!! :: " + nuts.getValue().getTraceabilityMatrix().get(source));
//                for (TraceabilityLink link : nuts.getValue().getTraceabilityMatrix().get(source)) {
//                    addLog(BuildAnalysisAction.class, "" + link.getTarget() + " ->" + target + ": " + (link.getTarget().equals(target)));
//                }
                TraceabilityLink link = ModelHelper.findLink(nuts.getValue().getTraceabilityMatrix(), source, target);
                addLog(BuildAnalysisAction.class, "ASM2.1!!! :: " + link);
                if (link != null) {
                    TraceabilityLink updated = updateFeedback(link, confidence, artifactsFolder);

                    // Update not linked req2src - req
                    // Update not linked req2src - src

                    // Update not linked req2test - req
                    // Update not linked req2test - test

                    precomputed = true;
                }
            }

            if (!precomputed) {
                // TODO: handle this case
            }

        }

        rsp.sendRedirect(".");
    }

    private TraceabilityLink updateFeedback(TraceabilityLink link, String confidence, File artifactsFolder) {
        File dbFullPath = new File(artifactsFolder.getAbsolutePath() + File.separator + DB);
        //Update asm
        TraceabilityLink asmLink = ModelHelper.findLink(asm.getTraceabilityMatrix(), link);
        asmLink.setFeedback(link.getSimilarity());
        asmLink.setConfidence(LikertScale.getLikerScale(Integer.parseInt(confidence)));
        addLog(BuildAnalysisAction.class, "!db.exists() :: " + (!dbFullPath.exists()));
        //Save link in the database
        FileHelper.saveLink(asmLink, dbFullPath, this);
        // Update hashMap
        Set<TraceabilityLink> possible = asm.getTraceabilityMatrix().get(link.getSource());
        possible.add(asmLink);
        return asmLink;
    }

    public void doReloadAsm(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        //addLog(BuildAnalysisAction.class, "Is build null?" + (build == null));
//        Action action = build.getAction(ProjectAnalysisAction.class);
        DescribableList<Publisher, Descriptor<Publisher>> publishersList = build.getProject().getPublishersList();
        ProjectAnalysisPublisher projectAnalysisPublisher = publishersList.get(ProjectAnalysisPublisher.class);

        File workspace = null;
        try {
            workspace = new File(build.getWorkspace().toURI());
            String sourceCodePath = workspace.getAbsolutePath() + File.separator + projectAnalysisPublisher.getSourcePath();
            String requirementsPath = projectAnalysisPublisher.getRequirementsPath() + File.separator;


            File artifactsFolder = new File(build.getArtifactManager().root().toURI());
            String asm1Result = artifactsFolder.getAbsolutePath() + File.separator + "VI.tm";
            HashMap<String, String> sourceCodeList = new HashMap<>();
            FileHelper.findAllFiles(new File(sourceCodePath), sourceCodeList);


            // Update asm model
            Map.Entry<String, IRModel> viResult = ModelHelper.loadIRModel(new File(asm1Result), sourceCodeList, workspace);
            setAsm(viResult.getValue());
            Set<Artifact> artifactsResult = viResult.getValue().getTraceabilityMatrix().keySet();

            // Update modals for requirements
            getModals().clear();
            for (Artifact artifact : artifactsResult) {
                String content = FileHelper.readFile(requirementsPath + artifact.getNameId());
                getModals().add(HtmlHelper.getModalRequirementHtml(HtmlHelper.getIdModal(artifact), artifact.getNameId(), content));
            }
            // Update modals for developer feedback
            getModalsDeveloper().clear();
            for (Map.Entry<Artifact, Set<TraceabilityLink>> entry : viResult.getValue().getTraceabilityMatrix().entrySet()) {
                for (TraceabilityLink link : entry.getValue()) {
                    getModalsDeveloper().add(HtmlHelper.getModalFeedbackHtml(HtmlHelper.getIdModalDeveloper(link), link.getSource().getNameId(), link.getTarget().getNameId()));
                }
            }

            File dbFullPath = new File(artifactsFolder.getAbsolutePath() + File.separator + DB);

            // Update asm feedback
            DatabaseXml feedbackLinks = FileHelper.loadLinks(dbFullPath, this);
            for (TraceabilityLink feedbackLink : feedbackLinks.getLinks()) {
                Set<TraceabilityLink> possibleLinks = asm.getTraceabilityMatrix().get(feedbackLink.getSource());

                for (TraceabilityLink link : possibleLinks) {
                    if (link.equals(feedbackLink)) {
                        link.setFeedback(feedbackLink.getFeedback());
                        link.setConfidence(feedbackLink.getConfidence());
                    }
                }
            }


            // Update asm2
            File asmTests = new File(artifactsFolder.getAbsolutePath() + File.separator + "VI-tests.tm");
            if (asmTests.exists()) {
                HashMap<String, String> testCodeList = new HashMap<>();
                FileHelper.findAllFiles(new File(workspace.getAbsolutePath() + File.separator + projectAnalysisPublisher.getTestPath()), testCodeList, ".c");
//                addLog(BuildAnalysisAction.class, "===========");
//                for (Map.Entry<String, String> entry:testCodeList.entrySet()) {
//                    addLog(BuildAnalysisAction.class, entry.getKey()+ " ::: "+ entry.getValue());
//                }
//                addLog(BuildAnalysisAction.class, "===========");

                Map.Entry<String, IRModel> viTests = ModelHelper.loadIRModel(asmTests, testCodeList, workspace);
                setAsm2(viTests.getValue());

                // Update modals for tests feedback
                getModalsTests().clear();
                for (Map.Entry<Artifact, Set<TraceabilityLink>> entry : viTests.getValue().getTraceabilityMatrix().entrySet()) {
                    for (TraceabilityLink link : entry.getValue()) {
//                        addLog(BuildAnalysisAction.class, link.getTarget().getNameId() + " :: "+link.getTarget().getPath());
                        getModalsTests().add(HtmlHelper.getModalFeedbackTestsHtml(HtmlHelper.getIdModalDeveloper(link), link.getSource().getNameId(), link.getTarget().getNameId()));
                    }
                }

                // Load test cases to source code
                String traces = projectAnalysisPublisher.getTraces();//"/Users/cbernalc/eclipse-workspace/cisco/Security-Traceability/datasets/LibEST_semeru_format/execution_traces.txt";
                Set<TraceabilityLink> fullSrc = asm.getTraceabilityMatrix().entrySet().iterator().next().getValue();

//                addLog(BuildAnalysisAction.class, "===========");
//                for (TraceabilityLink entry:fullSrc) {
//                    addLog(BuildAnalysisAction.class, entry.getTarget().getNameId()+ " ::: "+ entry.getTarget().getPath());
//                }
//                addLog(BuildAnalysisAction.class, "===========");

                Set<TraceabilityLink> fullTests = asm2.getTraceabilityMatrix().entrySet().iterator().next().getValue();
                addLog(BuildAnalysisAction.class, fullSrc.toString());
                addLog(BuildAnalysisAction.class, fullTests.toString());
                test2src = FileHelper.loadTraces(traces, fullSrc, fullTests);

                // Load artifacts not filtered
                fillNoSrc();
                fillNoTest();

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            addLog(BuildAnalysisAction.class, "InterruptedException");
            addLog(BuildAnalysisAction.class, Arrays.toString(e.getStackTrace()));
        }
        setFinished(true);
        if (rsp != null) {
            rsp.sendRedirect(".");
        }
    }

    private void fillNoTest() {
        req2TestNotLinkedReq.clear();

        for (Map.Entry<Artifact, Set<TraceabilityLink>> entry : asm2.getTraceabilityMatrix().entrySet()) {
            Set<TraceabilityLink> value = entry.getValue();
            boolean notLinked = true;
            for (TraceabilityLink link: value) {
                notLinked &= link.getFinalValue() < 0.4;
            }
            if(notLinked){
                req2TestNotLinkedReq.add(entry.getKey().getNameId());
            }
        }
    }

    private void fillNoSrc() {
        req2SrcNotLinkedReq.clear();

        for (Map.Entry<Artifact, Set<TraceabilityLink>> entry : asm.getTraceabilityMatrix().entrySet()) {
            Set<TraceabilityLink> value = entry.getValue();
            boolean notLinked = true;
            for (TraceabilityLink link: value) {
                notLinked &= link.getFinalValue() < 0.4;
            }
            if(notLinked){
                req2SrcNotLinkedReq.add(entry.getKey().getNameId());
            }
        }
    }

    public String getIdModal(Artifact req) {
        return HtmlHelper.getIdModal(req);
    }

    public String getIdModalDeveloper(TraceabilityLink link) {
        return HtmlHelper.getIdModalDeveloper(link);
    }

    public String getFeedbackValue(TraceabilityLink link) {
        if (link.getFeedback() != -1) {
            String likert = link.getConfidence().toString();
            return "<em>" + likert.charAt(0) + likert.substring(1).toLowerCase().replace("_", " ") + "</em>";
        }
        return "None";
    }

    public boolean isReady() {
        return finished && asm != null;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public List<String> getModals() {
        return modals;
    }

    public void setModals(List<String> modals) {
        this.modals = modals;
    }

    public List<String> getModalsDeveloper() {
        return modalsDeveloper;
    }

    public void setModalsDeveloper(List<String> modalsDeveloper) {
        this.modalsDeveloper = modalsDeveloper;
    }

    public List<String> getModalsTraces() {
        return modalsTraces;
    }

    public void setModalsTraces(List<String> modalsTraces) {
        this.modalsTraces = modalsTraces;
    }

    public String getConfidenceFilter() {
        return confidenceFilter;
    }

    public void setConfidenceFilter(String confidenceFilter) {
        this.confidenceFilter = confidenceFilter;
    }

    public int getLinkFilter() {
        return linkFilter;
    }

    public void setLinkFilter(int linkFilter) {
        this.linkFilter = linkFilter;
    }

    public IRModel getAsm2() {
        return asm2;
    }

    public void setAsm2(IRModel asm2) {
        this.asm2 = asm2;
    }

    public List<String> getModalsTests() {
        return modalsTests;
    }

    public void setModalsTests(List<String> modalsTests) {
        this.modalsTests = modalsTests;
    }

    public HashMap<Artifact, List<Artifact>> getTest2src() {
        return test2src;
    }

    public void setTest2src(HashMap<Artifact, List<Artifact>> test2src) {
        this.test2src = test2src;
    }

    public List<String> getReq2SrcNotLinkedReq() {
        return req2SrcNotLinkedReq;
    }

    public void setReq2SrcNotLinkedReq(List<String> req2SrcNotLinkedReq) {
        this.req2SrcNotLinkedReq = req2SrcNotLinkedReq;
    }

    public List<String> getReq2TestNotLinkedReq() {
        return req2TestNotLinkedReq;
    }

    public void setReq2TestNotLinkedReq(List<String> req2TestNotLinkedReq) {
        this.req2TestNotLinkedReq = req2TestNotLinkedReq;
    }
}
