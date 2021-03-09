package io.jenkins.plugins.traceability.helper;

import io.jenkins.plugins.traceability.BuildAnalysisAction;
import io.jenkins.plugins.traceability.model.Artifact;
import io.jenkins.plugins.traceability.model.AssociationModel;
import io.jenkins.plugins.traceability.model.IRModel;
import io.jenkins.plugins.traceability.model.TraceabilityLink;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ThreadExecutor extends Thread {

    private static final int SAMPLE_NUMBER = 3;
//    private static final int SAMPLE_NUMBER = 39;
    private List<String> command;
    private BuildAnalysisAction buildAnalysisAction;

    public ThreadExecutor(List<String> command, BuildAnalysisAction buildAnalysisAction) {
        this.command = command;
        this.buildAnalysisAction = buildAnalysisAction;
    }

    @Override
    public void run() {
        buildAnalysisAction.setFinished(false);
        buildAnalysisAction.setCurrentState("Computing IR techniques");
        CommandExecutor executor = new CommandExecutor(command);
        try {
//            buildAnalysisAction.addLog(ThreadExecutor.class, Arrays.toString(command.toArray()));
            String computeSimilarity = command.get(1);
            String sourceCodePath = command.get(4);
            File workspace = new File(buildAnalysisAction.getBuild().getWorkspace().toURI());
            if(!PermissionHelper.canRead(computeSimilarity)){
                throw new Exception("There are no permissions to access the file");
            }
            executor.executeCommand();
//            Thread.sleep(1000 * 20);

//            buildAnalysisAction.addLog(ThreadExecutor.class, executor.getStandardOutput().toString());
//            buildAnalysisAction.addLog(ThreadExecutor.class, executor.getErrorOutput().toString());
            buildAnalysisAction.addLog(ThreadExecutor.class, "1. IR techniques executed");

            String artifacts = command.get(command.size() - 1);
//            File asmFolder = new File(new File(artifacts).getParent() + File.separator + "asm");
//            if (!asmFolder.isDirectory()) {
//                boolean success = asmFolder.mkdirs();
//                if (!success) {
//                    buildAnalysisAction.addLog(ThreadExecutor.class, "Can't create asmFolder directory at "
//                            + asmFolder.getAbsolutePath());
//                }
//            }
//
            HashMap<String, String> sourceCodeList = new HashMap<>();
            FileHelper.findAllFiles(new File(sourceCodePath),sourceCodeList);

            AssociationModel asm1 = ModelHelper.loadAssociationModel("ASM1", new File(artifacts), sourceCodeList, workspace);
            AssociationModel sampling = ModelHelper.sampleModels(SAMPLE_NUMBER, asm1, new File(artifacts));

            String files = "";

            for (IRModel model : sampling.getModels()) {
                files += model.getFileName() + ",";
            }

            buildAnalysisAction.setCurrentState("Computing Association Model");
            String python3 = command.get(0);
            String pathRequirements = new File(command.get(3)) + File.separator;
            String causalRootPath = new File(command.get(1)).getParentFile().getAbsolutePath();
            command = Arrays.asList(python3, causalRootPath + File.separator + "ComputeAsm1.py", files.substring(0, files.length() - 1), artifacts);

            buildAnalysisAction.addLog(ThreadExecutor.class, Arrays.toString(command.toArray()));
            executor = new CommandExecutor(command);
            executor.executeCommand();


//            buildAnalysisAction.addLog(ThreadExecutor.class, "============================");
//            for (TraceabilityLink link : asm1.getModels().iterator().next().getLinks()) {
//                buildAnalysisAction.addLog(ThreadExecutor.class, link.getTarget() + " :: " + link.getTarget().getPath());
//
//            }
//            buildAnalysisAction.addLog(ThreadExecutor.class, "============================");
//            for (TraceabilityLink link : sampling.getModels().iterator().next().getLinks()) {
//                buildAnalysisAction.addLog(ThreadExecutor.class, link.getTarget() + " :: " + link.getTarget().getPath());
//
//            }
//            buildAnalysisAction.addLog(ThreadExecutor.class, "============================");

            buildAnalysisAction.addLog(ThreadExecutor.class, executor.getStandardOutput().toString());
//            buildAnalysisAction.addLog(ThreadExecutor.class, executor.getErrorOutput().toString());
            buildAnalysisAction.addLog(ThreadExecutor.class, "2. Association Model 1 executed");
            buildAnalysisAction.doReloadAsm(null, null);
/*
            // Read results from association model 1
            String asm1Result = artifacts + "VI.tm";
            Map.Entry<String, IRModel> viResult = ModelHelper.loadIRModel(new File(asm1Result), sourceCodeList, workspace);

//            HashMap<String, String> sourceCodeList = new HashMap<>();
//            FileHelper.findAllFiles(new File(sourceCodePath),sourceCodeList);

////             Fix full path for source code
//            FileHelper.addTargetFile2Artifacts(viResult.getValue(), sourceCodePath, workspace);
            buildAnalysisAction.setAsm(viResult.getValue());
            Set<Artifact> artifactsResult = viResult.getValue().getTraceabilityMatrix().keySet();
            buildAnalysisAction.getModals().clear();
            for (Artifact artifact : artifactsResult) {
                String content = FileHelper.readFile(pathRequirements + artifact.getNameId());
                buildAnalysisAction.getModals().add(HtmlHelper.getModalRequirementHtml(buildAnalysisAction.getIdModal(artifact.getNameId()), artifact.getNameId(), content));
            }
*/
            buildAnalysisAction.addLog(ThreadExecutor.class, "Thread finished");
            buildAnalysisAction.setCurrentState("");
        } catch (IOException e) {
            buildAnalysisAction.addLog(ThreadExecutor.class, "IOException");
            buildAnalysisAction.addLog(ThreadExecutor.class, Arrays.toString(e.getStackTrace()));
            buildAnalysisAction.setFinished(true);
            e.printStackTrace();
        } catch (Exception e) {
            buildAnalysisAction.addLog(ThreadExecutor.class, "Exception");
            buildAnalysisAction.addLog(ThreadExecutor.class, Arrays.toString(e.getStackTrace()));
            buildAnalysisAction.setFinished(true);
            e.printStackTrace();
        }
    }
}
