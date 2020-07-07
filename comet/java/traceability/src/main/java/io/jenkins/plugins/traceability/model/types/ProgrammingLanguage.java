package io.jenkins.plugins.traceability.model.types;

public enum ProgrammingLanguage {
    C("C"), JAVA("java");

    private String text;

    ProgrammingLanguage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
