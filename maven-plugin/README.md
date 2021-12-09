# Maven Plugin
The plugin's `generator` goal runs within Maven's default lifecycle in phase `generate-test-sources` (run `mvn generate-test-sources` to see the generator's log).
It generates the test code under `target/bpmndt` and adds this directory as test source directory, which is automatically compiled during the `test-compile` phase.
The compilation results (test cases and [API classes](src/main/java/org/camunda/community/bpmndt/api)) will be available in the test classpath afterwards.

:warning: With **IntelliJ IDEA**, you may need to [add the additional test source directory manually](https://www.jetbrains.com/help/idea/testing.html#add-test-root).
Right click on `target/bpmndt` > `Mark Directory as` > `Test Sources Root`

## Usage

```xml
<plugin>
  <groupId>org.camunda.community</groupId>
  <artifactId>bpmn-driven-testing-maven-plugin</artifactId>
  <version>0.4.1</version>
  <executions>
    <execution>
      <goals>
        <goal>generator</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Please see [Maven Central](https://search.maven.org/artifact/org.camunda.community/bpmn-driven-testing-maven-plugin) to get a specific version.

## Configuration
Available parameters for the plugin's `generator` goal:

| Parameter            | Type         | Description                                                                | Default value |
|:---------------------|:-------------|:---------------------------------------------------------------------------|:--------------|
| packageName          | String       | Package name, used for the generated test sources                          | generated     |
| processEnginePlugins | List<String> | List of process engine plugins to register at the process engine           | -             |
| springEnabled        | Boolean      | Enables Spring based testing                                               | false         |
| testSourceDirectory  | String       | Name of the directory under `target/`, used for the generated test sources | bpmndt        |

## Dependencies
Add dependencies, which are required to execute the generated test code:

```xml
<dependency>
  <groupId>org.camunda.bpm</groupId>
  <artifactId>camunda-engine</artifactId>
</dependency>

<!-- Required for plugin version 0.3.0 and lower -->
<dependency>
  <groupId>org.camunda.bpm</groupId>
  <artifactId>camunda-engine-plugin-spin</artifactId>
</dependency>

<dependency>
  <groupId>junit</groupId>
  <artifactId>junit</artifactId>
  <version>${junit.version}</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <version>1.4.197</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>org.camunda.bpm.assert</groupId>
  <artifactId>camunda-bpm-assert</artifactId>
  <version>${camunda.bpm.assert.version}</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.assertj</groupId>
  <artifactId>assertj-core</artifactId>
  <version>${assertj.version}</version>
  <scope>test</scope>
</dependency>
```

For **Spring** based testing, additional dependencies are required:

```xml
<dependency>
  <groupId>org.camunda.bpm</groupId>
  <artifactId>camunda-engine-spring</artifactId>
</dependency>

<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-beans</artifactId>
  <version>${spring.version}</version>
</dependency>
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-jdbc</artifactId>
  <version>${spring.version}</version>
</dependency>
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-context</artifactId>
  <version>${spring.version}</version>
</dependency>

<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-test</artifactId>
  <version>${spring.version}</version>
  <scope>test</scope>
</dependency>
```

Recommended versions:

| Dependency         | Version |
|:-------------------|:--------|
| Camunda BPM        | 7.15.0+ |
| Camunda BPM Assert | 10.0.0  |
| JUnit 4            | 4.13.2  |
| Assertj            | 3.18.1  |
| Spring Framework   | 5.2.8.RELEASE+ |

## Testing
:warning: This section is only important for plugin development!

Beside unit tests, a set of integration tests exist under [src/test/it](src/test/it).
These tests verify that the Maven plugin works correctly when executed within a Maven build.
The integration tests are implemented using the [Robot Framework](https://robotframework.org/) (Java implementation).

To execute unit and integration tests, run:

```
mvn clean integration-test
```

The Robot test report is written to `target/robot/report.html`.
