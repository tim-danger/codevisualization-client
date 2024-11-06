package com.dangersoft;

public class DiagramInformation {
    private String className;
    private String methodName;
    private String code;
    private boolean isClassDiagram;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isClassDiagram() {
        return isClassDiagram;
    }

    public void setClassDiagram(boolean classDiagram) {
        isClassDiagram = classDiagram;
    }

    @Override
    public String toString() {
        return "DiagramInformation{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", isClassDiagram=" + isClassDiagram +
                '}';
    }
}
