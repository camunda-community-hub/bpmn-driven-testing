* Settings
Library  OperatingSystem
Library  Process

* Test Cases
mvn clean test
  [Tags]  xignore
  ${result}=  Run process
  ...  mvn  -B  -f  ${CURDIR}/advanced-spring/pom.xml  clean  test  -Dplugin.version\=${VERSION}
  ...  shell=True  stdout=${TEMP}/advanced-spring.out  stderr=STDOUT

  Log  ${result.stdout}

  # plugin executed
  Should contain  ${result.stdout}  bpmn-driven-testing-maven-plugin:${VERSION}:generator

  ${testSources}  Set variable  ${CURDIR}/advanced-spring/target/bpmndt

  # Spring specific API classes written
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/AbstractJUnit4TestCase.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/cfg/SpringConfiguration.java

  File should exist  ${testSources}/org/camunda/community/bpmndt/api/AbstractJUnit4TestCase.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/cfg/SpringConfiguration.java

  # Spring configuration generated and written
  Should contain  ${result.stdout}  Generating Spring configuration
  Should contain  ${result.stdout}  Writing additional classes
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/BpmndtConfiguration.java

  File should exist  ${testSources}/generated/BpmndtConfiguration.java

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.AdvancedTest
  Should contain  ${result.stdout}  Running org.example.it.CustomProcessEnginePluginTest

  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0
