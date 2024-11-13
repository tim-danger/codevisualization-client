package com.dangersoft;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.cli.*;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Hello world!
 *
 */
public class App {

    private List<String> errorsWhenGeneratingDiagram = new ArrayList<>();
    private boolean checkForError = false;
    public static void main(String[] args) {
        CommandLine commandLine;

        // specify directory to uml.zip-file
        Option fileOption = Option.builder()
                .argName("directory")
                .hasArg()
                .desc("specify directory to uml.zip-file")
                .option("z")
                .longOpt("zip")
                .build();

        Option replacements = Option.builder()
                .argName("replacements")
                .hasArg()
                .desc("specify replacements to be replaced as comma-separated key / value pairs like key1=value1,key2=value2")
                .option("r")
                .longOpt("replace")
                .build();

        Option errorChecker = Option.builder()
                .argName("error-checker")
                .desc("checks if an error occurred during image processing")
                .option("c")
                .longOpt("check")
                .build();

        Options options = new Options();
        CommandLineParser parser = new DefaultParser();

        // String[] testArgs = { "-z", "C:/Projekte/codevisualization-client/src/main/resources", "-r", "skin rose=skin rose\n\rskinparam handwritten true" };
        // String[] testArgs = { "--zip", "C:/Projekte/codevisualization-client/src/main/resources", "--replace", "skin rose=skin rose\n\rskinparam handwritten true" };
        options.addOption(fileOption);
        options.addOption(replacements);
        options.addOption(errorChecker);

        App app = new App();

        try {
            commandLine = parser.parse(options, args);

            Map<String, String> mapWithReplacements = new HashMap<>();

            String directory = "";
            if (commandLine.hasOption(fileOption.getOpt())) {
                directory = commandLine.getOptionValue(fileOption.getOpt());
            } else {
                // es muss ein Directory übergeben werden, ggf. könnte man hier das aktuelle DIR angeben
                return;
            }

            if (commandLine.hasOption(errorChecker.getOpt())) {
                app.checkForError = true;
            } else {
                // es muss ein Directory übergeben werden, ggf. könnte man hier das aktuelle DIR angeben
                return;
            }

            if (commandLine.hasOption(replacements.getOpt())) {
                app.setReplacements(mapWithReplacements, commandLine.getOptionValue(replacements.getOpt()));
                System.out.println("Replacements: " + mapWithReplacements);
            }

            app.processZip(directory, mapWithReplacements);

        }
        catch (ParseException exception) {
            System.out.print("Parse error: ");
            System.out.println(exception.getMessage());
        }
    }

    private void setReplacements(Map<String, String> mapWithReplacements, String replacements) {
        Arrays.stream(replacements.split(",")).forEach(s -> this.setKeyValuePair(mapWithReplacements, s));
    }

    private void setKeyValuePair(Map<String, String> mapWithReplacements, String value) {
        String[] split = value.split("=");
        if (split.length == 2) {
            mapWithReplacements.put(split[0], split[1]);
        }
    }

    public void testMyMethod() {
        App app = new App();
        // Map<String, String> replacements = Map.of("skin rose", "skin rose\n\rskinparam handwritten true");
        // Map<String, String> replacements = Map.of("skin rose", "skinparam monochrome true\n\rskinparam shadowing true\n\rskinparam dpi 300\n\rskinparam handwritten true");
        // Map<String, String> replacements = Map.of("skin rose", "skinparam monochrome true\n\rskinparam shadowing true");
        ZipOutputResult result = app.readZipFile(app.resourceToFile("uml.zip").getPath(), DiagramType.PLANT_UML, Collections.emptyMap());
        app.saveOutput("src/main/resources", result);
    }

    public void processZip(String directory, Map<String, String> replacements) {
        ZipOutputResult result = this.readZipFile(directory + "/uml.zip", DiagramType.PLANT_UML, replacements);
        this.saveOutput(directory, result);
    }

    public void saveOutput(String path, ZipOutputResult result) {
        // ZIP generieren und im vom User spezifizierten Pfad speichern
        try (FileOutputStream fos = new FileOutputStream(path + "/output.zip")) {
            fos.write(generateZipFileFromResult(result));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] generateZipFileFromResult(ZipOutputResult result) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try(ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (DiagramInformation diagram : result.getDiagrams()) {
                tryToWriteClassAsZipEntry(zos, diagram);
            }
            for (Map.Entry<String, byte[]> entry : result.getFiles().entrySet()) {
                tryToWriteClassAsZipEntry(zos, entry.getKey(), entry.getValue());
            }
            if (checkForError && errorsWhenGeneratingDiagram.size() > 0) {
                tryToWriteClassAsZipEntry(zos, "error.txt", errorsWhenGeneratingDiagram.stream().collect(Collectors.joining(System.lineSeparator())).getBytes(StandardCharsets.UTF_8));
            }
        } catch(IOException ioe) {
            throw new RuntimeException("Could not generate ZIP-File");
        }
        return baos.toByteArray();
    }

    private void tryToWriteClassAsZipEntry(ZipOutputStream zos, String name, byte[] content) {
        try {
            ZipEntry entry = new ZipEntry(name);
            zos.putNextEntry(entry);
            zos.write(content);
            zos.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void tryToWriteClassAsZipEntry(ZipOutputStream zos, DiagramInformation diagram) {
        String basePath = diagram.getClassName() != null ? diagram.getClassName() + "/" : "";
        try {
            ZipEntry entry = new ZipEntry(basePath + diagram.getMethodName() + ".svg");
            zos.putNextEntry(entry);
            zos.write(generatePlantUmlImage(diagram.getCode(), diagram.getClassName(), diagram.getMethodName()));
            zos.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] generatePlantUmlImage(String code, String className, String methodName) {
        SourceStringReader reader = new SourceStringReader(code);
        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
            byte[] result = os.toByteArray();
            if (this.checkForError && containsError(result)) {
                this.errorsWhenGeneratingDiagram.add("Sequence diagram for class " + className + " method " + methodName + " contains an error!");
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean containsError(byte[] result) {
        return new String(result, StandardCharsets.UTF_8).contains("Syntax Error?");
    }

    public ZipOutputResult readZipFile(String pathToFile, DiagramType type, Map<String, String> replacements) {
        ZipOutputResult result = new ZipOutputResult();
        List<DiagramInformation> diagrams = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(pathToFile))){
            ZipEntry entry;
            while((entry = zip.getNextEntry()) != null) {
                String entryName = entry.getName();
                // Prüfung auf entsprechende Files
                if (!entryName.endsWith(type.getValue()) && !fileToKeep(entryName)) {
                    continue;
                }
                String[] parts = entryName.split("/");
                if (entryName.endsWith(type.getValue())) {
                    DiagramInformation diagram = new DiagramInformation();
                    if (parts.length > 2) {
                        // Sequenzdiagramm
                        diagram.setClassDiagram(false);
                        diagram.setClassName(parts[0]);
                        diagram.setMethodName(parts[1]);
                        diagram.setCode(replacementsInCode(new String(zip.readAllBytes(), StandardCharsets.UTF_8), replacements));
                        diagrams.add(diagram);
                    } else {
                        // Klassendiagramm
                        diagram.setClassDiagram(true);
                        diagram.setMethodName("class-diagram");
                        diagram.setCode(replacementsInCode(new String(zip.readAllBytes(), StandardCharsets.UTF_8), replacements));
                        diagrams.add(diagram);
                    }
                } else {
                    result.addFile(entryName, zip.readAllBytes());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        result.setDiagrams(diagrams);
        return result;
    }

    private boolean fileToKeep(String entryName) {
        // Error-Log oder Complexity-Report wollen wir behalten
        return entryName.endsWith(".txt") || entryName.endsWith(".log");
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
