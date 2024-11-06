package com.dangersoft;

public enum DiagramType {
    PLANT_UML(".puml"), D2(".d2"), MERMAID(".mm");
    private String value;
    DiagramType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
