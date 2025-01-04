# Gradle Plugin
The plugin `org.camunda.community.bpmndt` registers the Gradle task `generateTestCases`.
It generates the test code under `build/bpmndt` and adds this directory as test source directory, which is automatically compiled before the `compileTestJava` task of Gradle's Java plugin.
The compilation results (test cases and [API classes](../impl/src/main/java/org/camunda/community/bpmndt/api)) will be available in the test classpath afterwards.

:warning: Within **Eclipse**, the Gradle task `generateTestCases` must be executed manually to generate the test cases:

1. Create a new Gradle task configuration via `Run` > `Run Configurations...` > `Gradle Task` > `New Configuration`
2. Choose the working directory via `Workspace...`
3. Add the `generateTestCases` task
4. Run the Gradle task configuration

:warning: Within **IntelliJ IDEA**, the Gradle task `generateTestCases` must be executed manually to generate the test cases - see [Run Gradle tasks](https://www.jetbrains.com/help/idea/work-with-gradle-tasks.html#gradle_tasks):

1. Type

```
<CTRL><CTRL>
gradle generateTestCases
```

2. Right click the `build/bpmndt` test sources folder and perform a `Reload from Disk`

## Usage

In `settings.gradle`, define the plugin and a resolution strategy, since the plugin is provided as a Maven artfiact via Maven Central:

```groovy
pluginManagement {
  plugins {
    id 'org.camunda.community.bpmndt'
  }

  resolutionStrategy {
    eachPlugin {
      if (requested.id.toString() == 'org.camunda.community.bpmndt') {
        useModule("org.camunda.community:bpmn-driven-testing-gradle-plugin:1.0.0")
      }
    }
  }

  repositories {
    mavenCentral()
  }
}
```

In `build.gradle`, add the plugin beside the `java` Gradle plugin:

```groovy
plugins {
  id 'java'
  id 'org.camunda.community.bpmndt'
}
```

Please see [Maven Central](https://central.sonatype.com/artifact/org.camunda.community/bpmn-driven-testing-gradle-plugin/versions) to get a specific version.

## Configuration
Available properties:

| Parameter            | Type         | Description                                                                | Default value |
|:---------------------|:-------------|:---------------------------------------------------------------------------|:--------------|
| packageName          | String       | Package name, used for the generated test sources | generated     |
| processEnginePlugins | List<String> | List of process engine plugins to register at the process engine (not required for Spring Boot, since process engine plugins must be exposed as beans) | -             |
| springEnabled        | Boolean      | Enables Spring based testing (not required for Spring Boot, since here only the [BpmndtProcessEnginePlugin](../impl/src/main/java/org/camunda/community/bpmndt/api/cfg/BpmndtProcessEnginePlugin.java) must be exposed as a bean) | false |

The plugin's configuration is done in `build.gradle` within the `bpmndt` extension element:

```groovy
bpmndt {
  packageName = 'generated'
  processEnginePlugins = []
  springEnabled = false
}
```

## Dependencies
Add dependencies, which are required to execute the generated test code:

```groovy
dependencies {
  implementation 'org.camunda.bpm:camunda-engine:7.22.0'

  testImplementation 'com.h2database:h2:2.2.224'
  testImplementation 'org.assertj:assertj-core:3.26.3'
  testImplementation 'org.camunda.bpm.assert:camunda-bpm-assert:15.0.0'
  testImplementation 'org.junit.jupiter:junit-jupiter-api:5.10.1'

  testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.10.1'
}

test {
  useJUnitPlatform()
}
```

For **Spring** based testing, additional dependencies are required:

```groovy
dependencies {
  implementation 'org.camunda.bpm:camunda-engine-spring:7.22.0'
  implementation 'org.springframework:spring-beans:6.1.14'
  implementation 'org.springframework:spring-context:6.1.14'
  implementation 'org.springframework:spring-jdbc:6.1.14'

  testImplementation 'org.springframework:spring-test:6.1.14'
}
```

For **Spring Boot** based testing, additional dependencies are required:

```groovy
dependencies {
  implementation 'org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter:7.22.0'

  testImplementation 'org.springframework.boot:spring-boot-starter-test:3.3.4'
}
```

Recommended versions:

| Dependency         | Version |
|:-------------------|:--------|
| Camunda BPM        | 7.22.0  |
| Camunda BPM Assert | 15.0.0  |
| JUnit 5 (Jupiter)  | 5.10.1   |
| Assertj            | 3.26.3  |
| Spring Framework   | 6.1.14  |
| Spring Boot        | 3.3.4  |

## Development
:warning: This and the subsequent sections are only important for Gradle plugin development!

Since the latest Gradle dependencies are not available via Maven Central or other remote repositories,
this module uses a [local Maven repository](local-repository) that contains the required dependencies.
For the development within an IDE, it is recommended to add the `lib/` directory of a local Gradle installation manually to the classpath.

The development is done with Gradle in version `8.5`.

## Testing
Beside unit tests, a set of [integration tests](../integration-tests) exist,
which verify that the Gradle plugin works correctly when executed within a Gradle build.
The integration tests are implemented using the [Robot Framework](https://robotframework.org/) (Java implementation).

To execute unit and integration tests, run:

```sh
mvn clean install -pl gradle-plugin -am
```

The Robot test report is written to `target/robot/report.html`.
