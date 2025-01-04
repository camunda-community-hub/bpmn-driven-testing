# Maven Plugin for Camunda 8
The plugin's `generator` goal runs within Maven's default lifecycle in phase `generate-test-sources` (run `mvn generate-test-sources` to see the generator's log).
It generates the test code under `target/bpmndt` and adds this directory as test source directory, which is automatically compiled during the `test-compile` phase.
The compilation results (test cases and [API classes](../impl-8/src/main/java/org/camunda/community/bpmndt/api)) will be available in the test classpath afterwards.

:warning: Within **IntelliJ IDEA**, [add the additional test source directory manually](https://www.jetbrains.com/help/idea/testing.html#add-test-root).
Right click on `target/bpmndt` > `Mark Directory as` > `Test Sources Root`

## Usage

```xml
<plugin>
  <groupId>org.camunda.community</groupId>
  <artifactId>bpmn-driven-testing-8-maven-plugin</artifactId>
  <version>0.13.0</version>
  <executions>
    <execution>
      <goals>
        <goal>generator</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Please see [Maven Central](https://central.sonatype.com/artifact/org.camunda.community/bpmn-driven-testing-8-maven-plugin/versions) to get a specific version.

## Configuration
Available parameters for the plugin's `generator` goal:

| Parameter            | Type         | Description                                                                | Default value |
|:---------------------|:-------------|:---------------------------------------------------------------------------|:--------------|
| packageName          | String       | Package name, used for the generated test sources | generated |

## Dependencies
Add dependencies, which are required to execute the generated test code:

```xml
<dependency>
  <groupId>org.immutables</groupId>
  <artifactId>annotate</artifactId>
  <version>2.10.0</version>
  <scope>provided</scope>
</dependency>
<dependency>
  <groupId>org.immutables</groupId>
  <artifactId>value-annotations</artifactId>
  <version>2.10.0</version>
  <scope>provided</scope>
</dependency>

<!-- Test -->
<dependency>
  <groupId>io.camunda</groupId>
  <artifactId>zeebe-process-test-extension</artifactId>
  <version>${camunda.zeebe.version}</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter-api</artifactId>
  <version>${junit.jupiter.version}</version>
  <scope>test</scope>
</dependency>
```

Recommended versions:

| Dependency                   | Version |
|:-----------------------------|:--------|
| Zeebe Process Test Extension | 8.6.3   |
| JUnit 5 (Jupiter)            | 5.10.1  |

## Testing
:warning: This section is only important for Maven plugin development!

Beside unit tests, a set of [integration tests](../integration-tests-8) exist,
which verify that the Maven plugin works correctly when executed within a Maven build.
The integration tests are implemented using the [Robot Framework](https://robotframework.org/) (Java implementation).

To execute unit and integration tests, run:

```sh
mvn clean install -pl maven-plugin-8 -am
```

The Robot test report is written to `target/robot/report.html`.
