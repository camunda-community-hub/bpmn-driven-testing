# Maven Plugin
The plugin's `generator` goal runs within Maven's default lifecycle in phase `generate-test-sources` (run `mvn generate-test-sources` to see the generator's log).
It generates the test code under `target/bpmndt` and adds this directory as test source directory, which is automatically compiled during the `test-compile` phase.
The compilation results (test cases and [API classes](../impl/src/main/java/org/camunda/community/bpmndt/api)) will be available in the test classpath afterwards.

:warning: Within **IntelliJ IDEA**, [add the additional test source directory manually](https://www.jetbrains.com/help/idea/testing.html#add-test-root).
Right click on `target/bpmndt` > `Mark Directory as` > `Test Sources Root`

## Usage

```xml
<plugin>
  <groupId>org.camunda.community</groupId>
  <artifactId>bpmn-driven-testing-maven-plugin</artifactId>
  <version>0.8.0</version>
  <executions>
    <execution>
      <goals>
        <goal>generator</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Please see [Maven Central](https://central.sonatype.com/artifact/org.camunda.community/bpmn-driven-testing-maven-plugin/0.8.0/versions) to get a specific version.

## Configuration
Available parameters for the plugin's `generator` goal:

| Parameter            | Type         | Description                                                                | Default value |
|:---------------------|:-------------|:---------------------------------------------------------------------------|:--------------|
| jUnit5Enabled        | Boolean      | Enables JUnit 5 based test case generation | false |
| packageName          | String       | Package name, used for the generated test sources | generated     |
| processEnginePlugins | List<String> | List of process engine plugins to register at the process engine (not required for Spring Boot, since process engine plugins must be exposed as beans) | -             |
| springEnabled        | Boolean      | Enables Spring based testing (not required for Spring Boot, since here only the [BpmndtProcessEnginePlugin](../impl/src/main/java/org/camunda/community/bpmndt/api/cfg/BpmndtProcessEnginePlugin.java) must be exposed as a bean) | false |

## Dependencies
Add dependencies, which are required to execute the generated test code:

```xml
<dependency>
  <groupId>org.camunda.bpm</groupId>
  <artifactId>camunda-engine</artifactId>
</dependency>

<!-- Test -->
<dependency>
  <groupId>junit</groupId>
  <artifactId>junit</artifactId>
  <version>${junit.version}</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <version>${h2.version}</version>
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

For **JUnit 5** replace the `junit:junit` dependency with:

```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter-api</artifactId>
  <version>${junit.jupiter.version}</version>
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

<!-- Test -->
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-test</artifactId>
  <version>${spring.version}</version>
  <scope>test</scope>
</dependency>
```

For **Spring Boot** based testing, additional dependencies are required:

```xml
<dependency>
  <groupId>org.camunda.bpm.springboot</groupId>
  <artifactId>camunda-bpm-spring-boot-starter</artifactId>
</dependency>

<!-- Test -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>

<!-- Allows usage of JUnit 4 -->
<dependency>
  <groupId>org.junit.vintage</groupId>
  <artifactId>junit-vintage-engine</artifactId>
  <scope>test</scope>
  <exclusions>
    <exclusion>
      <groupId>org.hamcrest</groupId>
      <artifactId>hamcrest-core</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

Recommended versions:

| Dependency         | Version |
|:-------------------|:--------|
| Camunda BPM        | 7.19.0  |
| Camunda BPM Assert | 15.0.0  |
| JUnit 4            | 4.13.2  |
| JUnit 5 (Jupiter)  | 5.9.3   |
| Assertj            | 3.24.2  |
| Spring Framework   | 5.3.29  |
| Spring Boot        | 2.7.15  |

## Testing
:warning: This section is only important for Maven plugin development!

Beside unit tests, a set of [integration tests](../integration-tests) exist,
which verify that the Maven plugin works correctly when executed within a Maven build.
The integration tests are implemented using the [Robot Framework](https://robotframework.org/) (Java implementation).

To execute unit and integration tests, run:

```
mvn clean integration-test
```

The Robot test report is written to `target/robot/report.html`.
