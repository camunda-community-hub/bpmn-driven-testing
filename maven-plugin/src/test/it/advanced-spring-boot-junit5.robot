* Settings
Library  OperatingSystem
Library  Process

* Test Cases
mvn clean test
  [Tags]  xignore
  ${result}=  Run process
  ...  mvn  -B  -f  ${CURDIR}/advanced-spring-boot-junit5/pom.xml  clean  test  -Dplugin.version\=${VERSION}
  ...  shell=True  stdout=${TEMP}/advanced-spring-boot-junit5.out  stderr=STDOUT

  Log  ${result.stdout}

  # plugin executed
  Should contain  ${result.stdout}  bpmn-driven-testing-maven-plugin:${VERSION}:generator

  ${testSources}  Set variable  ${CURDIR}/advanced-spring-boot-junit5/target/bpmndt

  # Spring specific API classes written
  Should contain  ${result.stdout}  Writing file: target/bpmndt/org/camunda/community/bpmndt/api/AbstractJUnit5TestCase.java

  File should exist  ${testSources}/org/camunda/community/bpmndt/api/AbstractJUnit5TestCase.java

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.AdvancedTest

  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0
