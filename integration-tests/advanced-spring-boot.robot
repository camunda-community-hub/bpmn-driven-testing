* Settings
Library  OperatingSystem
Library  Process

* Test Cases
mvn clean test
  [Tags]  xignore  maven
  ${result}=  Run process
  ...  mvn  -B  -f  ${CURDIR}/advanced-spring-boot/pom.xml  clean  test  -Dplugin.version\=${VERSION}
  ...  shell=True  stdout=${TEMP}/advanced-spring-boot.out  stderr=STDOUT

  Log  ${result.stdout}

  # plugin executed
  Should contain  ${result.stdout}  bpmn-driven-testing:${VERSION}:generator

  Assert Test Code Generation  ${result}  target

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.ExampleTest
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0

gradle clean build
  [Tags]  xignore  gradle
  ${result}=  Run process
  ...  gradle  -p  ${CURDIR}/advanced-spring-boot  clean  build  -Pplugin.version\=${VERSION}  --info  --stacktrace
  ...  shell=True  stdout=${TEMP}/advanced-spring-boot.out  stderr=STDOUT

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

  ${testSources}  Set variable  ${CURDIR}/advanced-spring-boot/${buildDir}/bpmndt

  # Spring specific API classes written
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/AbstractJUnit5TestCase.java

  File should exist  ${testSources}/org/camunda/community/bpmndt/api/AbstractJUnit5TestCase.java
