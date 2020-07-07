package io.jenkins.plugins.traceability.helper;

import io.jenkins.plugins.traceability.model.Artifact;
import io.jenkins.plugins.traceability.model.TraceabilityLink;

public class HtmlHelper {

    public static String getModalRequirementHtml(String id, String title, String content){
        String html = "";
        html += "<div class=\"modal fade\" id=\""+id+"\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"exampleModalLabel\" aria-hidden=\"true\">" +
                "<div class=\"modal-dialog modal-lg\" role=\"document\">" +
                "<div class=\"modal-content\">" +
                "<div class=\"modal-header\">" +
                "<h5 class=\"modal-title\" id=\"exampleModalLabel\">"+title+"</h5>" +
                "<button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\">" +
                "<span aria-hidden=\"true\">&times;</span>" +
                "</button>" +
                "</div>" +
                "<div class=\"modal-body\">" +
                content +
                "</div>" +
                "<div class=\"modal-footer\">" +
                "<button type=\"button\" class=\"btn btn-secondary\" data-dismiss=\"modal\">Close</button>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>";
        return html;
    }

    public static String getModalFeedbackHtml(String id, String idSource, String idTarget) {
        return getModalFeedbackHtml(id,idSource,idTarget, "");
    }

    public static String getModalFeedbackTestsHtml(String id, String idSource, String idTarget) {
        return getModalFeedbackHtml(id,idSource,idTarget, "tests");
    }

    private static String getModalFeedbackHtml(String id, String idSource, String idTarget, String type) {
        String html = "";
        html += "<form method=\"POST\" action=\"feedback\" name=\"feedback\"><div class=\"modal fade\" id=\"" + id + "\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"exampleModalLabel\" aria-hidden=\"true\">" +
                "<div class=\"modal-dialog\" role=\"document\">" +
//                "<div class=\"modal-dialog modal-sm\" role=\"document\">" +
                "<div class=\"modal-content\">" +
                "<div class=\"modal-header\">" +
                "<h5 class=\"modal-title\" id=\"exampleModalLabel\">" + idSource + " - " + idTarget + "</h5>" +
                "<button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\">" +
                "<span aria-hidden=\"true\">&times;</span>" +
                "</button>" +
                "</div>" +
                "<div class=\"modal-body\">" +
                "Select whether you consider <span class=\"text-primary\">" + idSource + " - " + idTarget + "</span> is a link:" +
                "<div class=\"form-group\">\n" +
                "<select size=\"5\" class=\"form-control form-control-sm\" name=\"confidenceDeveloper\" id=\"confidenceDeveloper_" + idSource + "\">\n" +
                "<option value=\"1::" + idSource + "::" + idTarget +"::"+ type+ "\">Strongly disagree</option>\n" +
                "<option value=\"2::" + idSource + "::" + idTarget +"::"+ type+ "\">Disagree</option>\n" +
                "<option value=\"3::" + idSource + "::" + idTarget +"::"+ type+ "\">Undecided</option>\n" +
                "<option value=\"4::" + idSource + "::" + idTarget +"::"+ type+ "\">Agree</option>\n" +
                "<option value=\"5::" + idSource + "::" + idTarget +"::"+ type+ "\">Strongly agree</option>\n" +
                "</select>" +
                "</div>" +
                "</div>" +
                "<div class=\"modal-footer\">" +
                "<button type=\"button\" class=\"btn btn-secondary\" data-dismiss=\"modal\">Close</button>" +
                "<input type=\"submit\" class=\"btn btn-primary\" value=\"Save\"/>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div></form>";
        return html;
    }

    public static String getIdModalDeveloper(TraceabilityLink link){
        return "id_feedback_" + link.getSource().getNameId() + "__" + link.getTarget().getNameId();
    }

    public static String getIdModal(Artifact req) {
        return req.getNameId().substring(0, req.getNameId().lastIndexOf("."));
    }
}
