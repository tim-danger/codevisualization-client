# codevisualization-client
Client that turns UML-Code designed for rendering into svg / png files

First you are supposed to build the client using

```mvn clean install```

Next you can copy the resulting JAR-File to a destination of your choice and execute the following command:

```java -jar codevisualization-client-1.0-SNAPSHOT-jar-with-dependencies.jar -z 'C:/Projekte/codevisualization-client/src/main/resources'```

With the option ```-z``` you have to specify the path, which is exactly the path where the file ```uml.zip``` is located (this file is generated using the corresponding IntelliJ-Plugin, please check the market-place and download the plugin accordingly). This will create a file called ```output.zip```, which contains all the generated svg-files.

With the option ```-r``` you can specify replacements that will be performed before the svg-files are generated. Let's say, you want to generate Plant-UML-files that are drawn using the handwritten-mode. In this case you can replace existing settings:

```
skin rose
```

has to be replaced by

```
skin rose
skinparam handwritten true
```

To achieve this, we will have to set a replacement that contains the linebreak, otherwise we wouldn't be able to get the result we want.

```java -jar codevisualization-client-1.0-SNAPSHOT-jar-with-dependencies.jar --replace 'skin rose=skin rose```

```skinparam handwritten true' --zip 'C:/Projekte/codevisualization-client/src/main/resources'```

Oder alternativ:

```java -jar codevisualization-client-1.0-SNAPSHOT-jar-with-dependencies.jar -r 'skin rose=skin rose\n\rskinparam handwritten true' -z 'C:/Projekte/codevisualization-client/src/main/resources'```

