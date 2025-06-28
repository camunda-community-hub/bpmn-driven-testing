* Settings
Library  OperatingSystem
Library  Process

* Test Cases
mvn clean test
  [Tags]  xignore  maven
  ${result}=  Run process
  ...  mvn  -B  -f  ${CURDIR}/advanced-spring/pom.xml  clean  test  -Dplugin.version\=${VERSION}
  ...  shell=True  stdout=${TEMP}/advanced-spring.out  stderr=STDOUT

  Log  ${result.stdout}

  # plugin executed
  Should contain  ${result.stdout}  bpmn-driven-testing:${VERSION}:generator

  Assert Test Code Generation  ${result}  target

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.AdvancedTest
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0

gradle clean build
  [Tags]  xignore  gradle
  ${result}=  Run process
  ...  gradle  -p  ${CURDIR}/advanced-spring  clean  build  -Pplugin.version\=${VERSION}  --info  --stacktrace
  ...  shell=True  stdout=${TEMP}/advanced-spring.out  stderr=STDOUT

  Log  ${result.stdout}

  # task executed
  Should contain  ${result.stdout}  > Task :generateTestCases

  Assert Test Code Generation  ${result}  build

  # tests executed
  Should contain  ${result.stdout}  finished executing tests
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0

* Keywords
Assert Test Code Generation
  [Arguments]  ${result}  ${buildDir}

  ${testSources}  Set variable  ${CURDIR}/advanced-spring/${buildDir}/bpmndt

  # Spring specific API classes written
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/AbstractJUnit5TestCase.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/cfg/SpringConfiguration.java

  File should exist  ${testSources}/org/camunda/community/bpmndt/api/AbstractJUnit5TestCase.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/cfg/SpringConfiguration.java

  # Spring configuration generated and written
  Should contain  ${result.stdout}  Generating Spring configuration
  Should contain  ${result.stdout}  Writing additional classes
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/BpmndtConfiguration.java

  File should exist  ${testSources}/generated/BpmndtConfiguration.java
