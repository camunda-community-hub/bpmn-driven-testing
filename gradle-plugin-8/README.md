# Gradle Plugin for Camunda 8
The plugin `org.camunda.community.bpmndt` registers the Gradle task `generateTestCases`.
It generates the test code under `build/bpmndt` and adds this directory as test source directory, which is automatically compiled before the `compileTestJava` task of Gradle's Java plugin.
The compilation results (test cases and [API classes](../impl-8/src/main/java/org/camunda/community/bpmndt/api)) will be available in the test classpath afterwards.

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
        useModule("org.camunda.community:bpmn-driven-testing-8-gradle-plugin:1.2.1")
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

Please see [Maven Central](https://central.sonatype.com/artifact/org.camunda.community/bpmn-driven-testing-8-gradle-plugin/versions) to get a specific version.

## Configuration
Available properties:

| Parameter            | Type         | Description                                                                | Default value |
|:---------------------|:-------------|:---------------------------------------------------------------------------|:--------------|
| packageName          | String       | Package name, used for the generated test sources | generated |

The plugin's configuration is done in `build.gradle` within the `bpmndt` extension element:

```groovy
bpmndt {
  packageName = 'generated'
}
```

## Dependencies
Add dependencies, which are required to execute the generated test code:

```groovy
dependencies {
  testImplementation 'io.camunda:zeebe-process-test-extension:8.7.6'
  testImplementation 'org.junit.jupiter:junit-jupiter-api:5.11.4'

  testCompileOnly 'org.immutables:annotate:2.10.0'
  testCompileOnly 'org.immutables:value-annotations:2.10.0'

  testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.11.4'
}

test {
  useJUnitPlatform()
}
```

Recommended versions:

| Dependency                   | Version |
|:-----------------------------|:--------|
| Zeebe Process Test Extension | 8.7.6  |
| JUnit 5 (Jupiter)            | 5.11.4  |

## Testing
:warning: This sections are only important for Gradle plugin development!

Beside unit tests, a set of [integration tests](../integration-tests-8) exist,
which verify that the Gradle plugin works correctly when executed within a Gradle build.
The integration tests are implemented using the [Robot Framework](https://robotframework.org/) (Java implementation).

To execute unit and integration tests, run:

```sh
mvn clean install -pl gradle-plugin-8 -am
```

The Robot test report is written to `target/robot/report.html`.
