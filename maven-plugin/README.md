# Maven Plugin

```xml
<plugin>
  <groupId>org.camunda.community</groupId>
  <artifactId>bpmn-driven-testing-maven-plugin</artifactId>
  <version>${plugin.version}</version>
  <executions>
    <execution>
      <goals>
        <goal>generator</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Dependencies, required to execute the generate test code:

```xml
<dependency>
  <groupId>junit</groupId>
  <artifactId>junit</artifactId>
  <version>${junit.version}</version>
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

## Configuration
Available parameters for the plugin's `generator` goal:

| Parameter           | Type    | Description                                                                |
|:--------------------|:--------|:---------------------------------------------------------------------------|
| packageName         | String  | Package name, used for the generated test sources                          |
| springEnabled       | Boolean | Enables Spring based testing                                               |
| testSourceDirectory | String  | Name of the directory under `target/`, used for the generated test sources |

## Testing
Beside unit tests, a set of integration tests exist under [src/test/it](src/test/it).
These tests verify the integration of the Maven plugin. The integration tests are implemented using the [Robot Framework](https://robotframework.org/).

To execute unit and integration tests, run:

```
mvn clean integration-test
```
