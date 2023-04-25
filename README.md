# JustinStr-New
#### 1. Introduction

Generating string test case for java project.

#### 2. How to use?

We now support usage from the command line.(**Windows or Mac**). 

The path of JustinStr.jar is **out/artifacts/JustinStr_jar**. 

You can run the JustinStr.jar through the following steps：

1. `cd out/artifacts/JustinStr_jar`

2. `java -jar JustinStr.jar --jre "jrePath" --input "inputPath" --output "outputPath"`
   - "jrePath" indicates the jre path in the local jdk.
   - "inputPath" indicates the input class file, note that the jar package under the windows system needs to be decompressed first.
   - "outputPath" indicates the output path of the result.

For more information, you can run `java -jar JustinStr.jar -h` or `java -jar JustinStr.jar --help`:

```
<options>      <description>
----------------------------
-h(--help)     help
--jre          local JRE path in JDK
--input        compiled class path under test
--output       output path folder
--maxString    maximum length of string
--minSet       minimum size of set/array
--maxSet       maximum size of set/array
--maxCase      maximum size of cases for each method
--maxTime      maximum generation time for each class
```

