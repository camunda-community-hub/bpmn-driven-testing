* Settings
Library  OperatingSystem
Library  Process

* Test Cases
mvn clean test
  [Tags]  xignore
  ${result}=  Run process
  ...  mvn  -B  -f  ${CURDIR}/advanced-spring/pom.xml  clean  package  -Dplugin.version\=${VERSION}
  ...  shell=True  stdout=${TEMP}/advanced-spring.out  stderr=STDOUT

  Log  ${result.stdout}

  # plugin executed
  Should contain  ${result.stdout}  bpmn-driven-testing-maven-plugin:${VERSION}:generator

  ${testSources}  Set variable  ${CURDIR}/advanced-spring/target/bpmndt

  # Spring specific API classes written
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/AbstractJUnit4SpringBasedTestRule.java
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/cfg/AbstractConfiguration.java

  File should exist  ${testSources}/org/camunda/community/bpmndt/api/AbstractJUnit4SpringBasedTestRule.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/cfg/AbstractConfiguration.java

  # Spring configuration generate and written
  Should contain  ${result.stdout}  Writing Spring configuration class
  Should contain  ${result.stdout}  Writing file: target/bpmndt/generated/BpmndtConfiguration.java

  File should exist  ${testSources}/generated/BpmndtConfiguration.java

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.AdvancedSpringTest
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0
