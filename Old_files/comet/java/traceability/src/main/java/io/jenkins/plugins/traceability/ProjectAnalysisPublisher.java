package io.jenkins.plugins.traceability;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.traceability.model.types.IRTechnique;
import io.jenkins.plugins.traceability.model.types.ProgrammingLanguage;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class ProjectAnalysisPublisher extends Recorder {

    private final String requirementsPath;
    private final String python3Path;
    private final String causalityPath;
    private final String sourcePath;
    private final String testPath;
    private final String projectLanguage;

    private final boolean vsm;
    private final boolean lsi;
    private final boolean js;
    private final boolean lda;
    private final boolean nmf;
    private final boolean vsm_lda;
    private final boolean js_lda;
    private final boolean vsm_nmf;
    private final boolean js_nmf;
    private final boolean vsm_js;

    private final String traces;


    @DataBoundConstructor
    public ProjectAnalysisPublisher(String requirementsPath, String python3Path, String causalityPath, String sourcePath, String testPath, String projectLanguage, boolean vsm, boolean lsi, boolean js, boolean lda, boolean nmf, boolean vsm_lda, boolean js_lda, boolean vsm_nmf, boolean js_nmf, boolean vsm_js, String traces) {
        this.requirementsPath = requirementsPath;
        this.python3Path = python3Path;
        this.causalityPath = causalityPath;
        this.sourcePath = sourcePath;
        this.testPath = testPath;
        this.projectLanguage = projectLanguage;
        this.vsm = vsm;
        this.lsi = lsi;
        this.js = js;
        this.lda = lda;
        this.nmf = nmf;
        this.vsm_lda = vsm_lda;
        this.js_lda = js_lda;
        this.vsm_nmf = vsm_nmf;
        this.js_nmf = js_nmf;
        this.vsm_js = vsm_js;
        this.traces = traces;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    private static Logger getLogger() {
        return Logger.getLogger(ProjectAnalysisPublisher.class.getName());
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new ProjectAnalysisAction(project);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        List<String> command = new ArrayList<>();

        String techniques = "";
        if (vsm)
            techniques += IRTechnique.VSM.name().toLowerCase() + ",";
        if (lsi)
            techniques += IRTechnique.LSI.name().toLowerCase() + ",";
        if (js)
            techniques += IRTechnique.JS.name().toLowerCase() + ",";
        if (lda)
            techniques += IRTechnique.LDA.name().toLowerCase() + ",";
        if (nmf)
            techniques += IRTechnique.NMF.name().toLowerCase() + ",";
        if (vsm_lda)
            techniques += IRTechnique.VSM_LDA.name().toLowerCase() + ",";
        if (vsm_js)
            techniques += IRTechnique.VSM_JS.name().toLowerCase() + ",";
        if (vsm_nmf)
            techniques += IRTechnique.VSM_NMF.name().toLowerCase() + ",";
        if (js_lda)
            techniques += IRTechnique.JS_LDA.name().toLowerCase() + ",";
        if (js_nmf)
            techniques += IRTechnique.JS_NMF.name().toLowerCase() + ",";

        if (!techniques.isEmpty()) {
            techniques = techniques.substring(0, techniques.length() - 1);
        } else {
            // validate this case before, make sure at least one option is selected in the configuration
        }

        //Dataset name
        //requirements folder
        //source path
        //programming language
        //techniques
        String projectName = build.getProject().getName();
//        String output = "/Users/cbernalc/eclipse-workspace/cisco/output-similarities/";

        File workspace = new File(build.getWorkspace().toURI());
        listener.getLogger().println(workspace.getAbsolutePath());
        Collection<File> listFiles = FileUtils.listFiles(workspace.getAbsoluteFile(), new String[]{"java"},
                true);
        listener.getLogger().println(Arrays.toString(listFiles.toArray()));
        //File artifactsDir = build.getArtifactsDir();
        //artifactsDir.getCanonicalPath();

//            for (File file : listFiles) {
//                files.add(file.getName() + "</br>");
//            }

//            files.add(build.getArtifactManager().root().toString());
//            files.add(build.getArtifactsDir().getCanonicalPath());
//            files.add(this.toString());

        // Test Jython here
//            PythonHelper helper = new PythonHelper();
//            files.add(helper.hello(build));
//            files.add(Arrays.toString(command.toArray()));
//            List<String> nltk = Arrays.asList(python3Path, "/Users/cbernalc/eclipse-workspace/cisco/Security-Traceability/python/Unified-traceability/unified_traceability/script_nltk.py");
//            CommandExecutor executor = new CommandExecutor(nltk);
//            executor.executeCommand();
//            files.add(executor.getStandardOutput().toString());
//            files.add(executor.getErrorOutput().toString());
//            files.add("=================================");

        // Make sure this exist
        File artifactsFolder = new File(build.getArtifactManager().root().toURI());
        if (!artifactsFolder.isDirectory()) {
            boolean success = artifactsFolder.mkdirs();
            if (!success) {
                listener.getLogger().println("Can't create artifacts directory at "
                        + artifactsFolder.getAbsolutePath());
            }
        }
        // use this folder in the execution
        command = Arrays.asList(python3Path, causalityPath + "/ComputeSimilarity.py", projectName, requirementsPath, workspace + File.separator + sourcePath, projectLanguage, techniques, artifactsFolder.getAbsolutePath() + File.separator);

//            files.add(executor.getStandardOutput().toString());
//            files.add(executor.getErrorOutput().toString());
//            files.add("Python executed");

//            files.add("Error in build " + build.number + "</br>");
//            files.add(e.getMessage());
//            files.add(e.toString());
//            files.add(Arrays.toString(e.getStackTrace()));

        BuildAnalysisAction action = new BuildAnalysisAction(command, build);
        action.computeAnalysis();
        build.addAction(action);
        return true;
    }

    public String getCausalityPath() {
        return causalityPath;
    }

    public String getRequirementsPath() {
        return requirementsPath;
    }

    public String getPython3Path() {
        return python3Path;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getProjectLanguage() {
        return projectLanguage;
    }

    public boolean getVsm() {
        return vsm;
    }

    public boolean getLsi() {
        return lsi;
    }

    public boolean getJs() {
        return js;
    }

    public boolean getLda() {
        return lda;
    }

    public boolean getNmf() {
        return nmf;
    }

    public boolean getVsm_lda() {
        return vsm_lda;
    }

    public boolean getJs_lda() {
        return js_lda;
    }

    public boolean getVsm_nmf() {
        return vsm_nmf;
    }

    public boolean getJs_nmf() {
        return js_nmf;
    }

    public boolean getVsm_js() {
        return vsm_js;
    }

    @Override
    public String toString() {
        return "ProjectAnalysisPublisher{" +
                "requirementsPath='" + requirementsPath + '\'' +
                ", python3Path='" + python3Path + '\'' +
                ", causalityPath='" + causalityPath + '\'' +
                ", sourcePath='" + sourcePath + '\'' +
                ", projectLanguage='" + projectLanguage + '\'' +
                ", vsm=" + vsm +
                ", lsi=" + lsi +
                ", js=" + js +
                ", lda=" + lda +
                ", nmf=" + nmf +
                ", vsm_lda=" + vsm_lda +
                ", js_lda=" + js_lda +
                ", vsm_nmf=" + vsm_nmf +
                ", js_nmf=" + js_nmf +
                ", vsm_js=" + vsm_js +
                '}';
    }

    public String getTestPath() {
        return testPath;
    }

    public String getTraces() {
        return traces;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
//            AbstractProject cast = jobType.cast(AbstractBuild.class);
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Traceability";
        }

        public ListBoxModel doFillProjectLanguageItems(@QueryParameter("projectLanguage") String projectLanguage) {
            ListBoxModel items = new ListBoxModel();
            if (projectLanguage == null) {
                projectLanguage = ProgrammingLanguage.C.getText();
            }
            for (ProgrammingLanguage language : ProgrammingLanguage.values()) {
                if (language.getText().equals(projectLanguage)) {
                    items.add(new ListBoxModel.Option(language.name(), language.getText(), true));
                } else {
                    items.add(language.name(), language.getText());
                }
            }
            return items;
        }

//        @Override
//        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
//            // To persist global configuration information,
//            // set that to properties and call save().
//            useFrench = formData.getBoolean("useFrench");
//            // ^Can also use req.bindJSON(this, formData);
//            // (easier when there are many fields; need set* methods for this, like
//            // setUseFrench)
//            save();
//            return super.configure(req, formData);
//        }
    }


}
