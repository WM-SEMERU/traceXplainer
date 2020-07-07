package io.jenkins.plugins.traceability;

import hudson.Extension;
import hudson.model.*;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.traceability.model.Artifact;
import io.jenkins.plugins.traceability.model.TraceabilityLink;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ProjectAnalysisAction implements Action, Describable<ProjectAnalysisAction> {

    private AbstractProject<?, ?> project;
    private List<TraceabilityLink> list = new ArrayList<>();
    private String myText;

    private String requirement = null;
    private String target = null;

    public ProjectAnalysisAction(AbstractProject<?, ?> project) {
        this.project = project;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return "/plugin/traceability/img/project_icon.png";
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "Association Model";
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "association-model";
    }

//    public AssociationModel getModel() {
//        BuildAnalysisAction action = project.getLastBuild().getAction(BuildAnalysisAction.class);
//        if (action != null) {
//            if (action.isFinished()) {
//
//            }
//        }
//        return new AssociationModel("This is a test", false);
//    }

    public void doAddItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        project.checkPermission(BuildableItem.BUILD);

//        List<ParameterValue> values = new ArrayList<ParameterValue>();

        JSONObject formData = req.getSubmittedForm();
        JSONObject listData = req.getSubmittedForm().getJSONObject("list");
        String source = formData.getString("source");
        String target = formData.getString("target");
        double link = .54321d;
        this.list.add(new TraceabilityLink(source, target, link));
        this.list.add(new TraceabilityLink(listData.toString()));
        this.list.add(new TraceabilityLink(formData.toString()));
//        if (!formData.isEmpty()) {
//            for (ParameterDefinition parameterDefinition : getParameterDefinitions()) {
//                ParameterValue parameterValue = parameterDefinition.createValue(req);
//                if (parameterValue.getClass().isAssignableFrom(BooleanParameterValue.class)) {
//                    boolean value = (req.getParameter(parameterDefinition.getName()) != null);
//                    parameterValue = ((BooleanParameterDefinition) parameterDefinition).createValue(String.valueOf(value));
//                } else if (parameterValue.getClass().isAssignableFrom(PasswordParameterValue.class)) {
//                    parameterValue = applyDefaultPassword(parameterDefinition, parameterValue);
//                }
//                // This will throw an exception if the provided value is not a valid option for the parameter.
//                // This is the desired behavior, as we want to ensure valid submissions.
//                values.add(parameterValue);
//            }
//        }

//        Jenkins.getInstance().getQueue().schedule(project, 0, new ParametersAction(values), new CauseAction(new Cause.UserIdCause()));
        rsp.sendRedirect(".");
    }

    private ParameterValue applyDefaultPassword(ParameterDefinition parameterDefinition, ParameterValue parameterValue) {
        return null;
    }

    public boolean isReady() {

        BuildAnalysisAction action = project.getLastBuild().getAction(BuildAnalysisAction.class);
        if (action != null && action.getAsm() != null) {
            HashMap<Artifact, Set<TraceabilityLink>> matrix = action.getAsm().getTraceabilityMatrix();
            return true;
        }


        return false;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<ProjectAnalysisAction> {
        public ListBoxModel doFillRequirementItems(@QueryParameter("requirement") String requirement) {
            ListBoxModel items = new ListBoxModel();

//            BuildAnalysisAction action = project.getLastBuild().getAction(BuildAnalysisAction.class);
//            if (action != null && action.getAsm() != null) {
//                HashMap<Artifact, List<TraceabilityLink>> matrix = action.getAsm().getTraceabilityMatrix();
//
//                if (requirement == null) {
//                    requirement = matrix.keySet().iterator().next().getNameId();
//                }
//                for (Artifact artifact : matrix.keySet()) {
//                    if (artifact.getNameId().equals(requirement)) {
//                        items.add(new ListBoxModel.Option(artifact.getNameId(), artifact.getNameId(), true));
//                    } else {
//                        items.add(artifact.getNameId(), artifact.getNameId());
//                    }
//                }
//            }
            items.add("test1", "test1");
            return items;

        }

        public ListBoxModel doFillTargetItems(@QueryParameter("requirement") String requirement, @QueryParameter("target") String target) {
            ListBoxModel items = new ListBoxModel();
//            BuildAnalysisAction action = project.getLastBuild().getAction(BuildAnalysisAction.class);
//            if (action != null && action.getAsm() != null) {
//                HashMap<Artifact, List<TraceabilityLink>> matrix = action.getAsm().getTraceabilityMatrix();
//
//                if (target == null) {
//                    target = matrix.get(target).iterator().next().getTarget().getNameId();
//                }
//                for (TraceabilityLink link : matrix.get(target)) {
//                    if (link.getTarget().getNameId().equals(target)) {
//                        items.add(new ListBoxModel.Option(link.getTarget().getNameId(), link.getTarget().getNameId(), true));
//                    } else {
//                        items.add(link.getTarget().getNameId(), link.getTarget().getNameId());
//                    }
//                }
//            }
            items.add("test1", "test1");
            return items;
        }
    }

    private ParameterDefinition[] getParameterDefinitions() {
        return null;
    }

    public List<TraceabilityLink> getList() {
        return list;
    }

    public void setList(List<TraceabilityLink> list) {
        this.list = list;
    }

    public String getMyText() {
        return myText;
    }

    public void setMyText(String myText) {
        this.myText = myText;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public Descriptor<ProjectAnalysisAction> getDescriptor() {
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            throw new IllegalStateException("Jenkins has not been started");
        }
        return jenkins.getDescriptorOrDie(getClass());
    }



}
