package com.dangersoft;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        App app = new App();
        // Map<String, String> replacements = Map.of("skin rose", "skin rose\n\rskinparam handwritten true");
        // Map<String, String> replacements = Map.of("skin rose", "skinparam monochrome true\n\rskinparam shadowing true\n\rskinparam dpi 300\n\rskinparam handwritten true");
        Map<String, String> replacements = Map.of("skin rose", "skinparam monochrome true\n\rskinparam shadowing true");
        List<DiagramInformation> result = app.readZipFile(app.resourceToFile("uml.zip").getPath(), DiagramType.PLANT_UML, replacements);
        app.saveOutput("src/main/resources", result);
    }

    public void saveOutput(String path, List<DiagramInformation> result) {
        // ZIP generieren und im vom User spezifizierten Pfad speichern
        try (FileOutputStream fos = new FileOutputStream(path + "/output.zip")) {
            fos.write(generateZipFileFromResult(result));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] generateZipFileFromResult(List<DiagramInformation> result) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (DiagramInformation diagram : result) {
                tryToWriteClassAsZipEntry(zos, diagram);
            }
        } catch(IOException ioe) {
            throw new RuntimeException("Could not generate ZIP-File");
        }
        return baos.toByteArray();
    }

    private void tryToWriteClassAsZipEntry(ZipOutputStream zos, DiagramInformation diagram) {
        String basePath = diagram.getClassName() + "/";
        try {
            ZipEntry entry = new ZipEntry(basePath + diagram.getMethodName() + ".svg");
            zos.putNextEntry(entry);
            zos.write(generatePlantUmlImage(diagram.getCode()));
            zos.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] generatePlantUmlImage(String code) {
        SourceStringReader reader = new SourceStringReader(code);
        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
            return os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<DiagramInformation> readZipFile(String pathToFile, DiagramType type, Map<String, String> replacements) {
        List<DiagramInformation> diagrams = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(pathToFile))){
            while(zip.getNextEntry() != null) {
                ZipEntry entry = zip.getNextEntry();
                if (entry == null) {
                    continue;
                }
                String entryName = entry.getName();
                if (!entryName.endsWith(type.getValue())) {
                    continue;
                }
                String[] parts = entryName.split("/");
                DiagramInformation diagram = new DiagramInformation();
                if (parts.length > 2) {
                    diagram.setClassDiagram(false);
                    diagram.setClassName(parts[0]);
                    diagram.setMethodName(parts[1]);
                    diagram.setCode(replacementsInCode(new String(zip.readAllBytes(), StandardCharsets.UTF_8), replacements));
                    diagrams.add(diagram);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return diagrams;
    }

    private String replacementsInCode(String code, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            code = code.replace(entry.getKey(), entry.getValue());
        }
        return code;
    }

    public File resourceToFile(String resource) {
        URL url = getClass().getClassLoader().getResource(resource);
        try {
            return new File(url.toURI().getPath());
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
