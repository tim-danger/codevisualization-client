package com.dangersoft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZipOutputResult {

    private List<DiagramInformation> diagrams = new ArrayList<>();
    private Map<String, byte[]> files = new HashMap<>();

    public List<DiagramInformation> getDiagrams() {
        return diagrams;
    }

    public void setDiagrams(List<DiagramInformation> diagrams) {
        this.diagrams = diagrams;
    }

    public Map<String, byte[]> getFiles() {
        return files;
    }

    public void setFiles(Map<String, byte[]> files) {
        this.files = files;
    }

    public void addFile(String fileName, byte[] bytes) {
        this.files.put(fileName, bytes);
    }
}
