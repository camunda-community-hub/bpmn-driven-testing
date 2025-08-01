* Settings
Library  OperatingSystem
Library  Process

* Test Cases
mvn clean test
  [Tags]  xignore  maven
  ${result}=  Run process
  ...  mvn  -B  -f  ${CURDIR}/external-task-client-spring-boot/pom.xml  clean  test  -Dplugin.version\=${VERSION}
  ...  shell=True  stdout=${TEMP}/external-task-client-spring-boot.out  stderr=STDOUT

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
  ...  gradle  -p  ${CURDIR}/external-task-client-spring-boot  clean  build  -Pplugin.version\=${VERSION}  --info  --stacktrace
  ...  shell=True  stdout=${TEMP}/external-task-client-spring-boot.out  stderr=STDOUT

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

  ${testSources}  Set variable  ${CURDIR}/external-task-client-spring-boot/${buildDir}/bpmndt

  # should detect external task client
  Should contain  ${result.stdout}  Found external task client

  # BPMN files found
  Should contain  ${result.stdout}  Found BPMN file: example.bpmn

  # test cases generated
  Should contain  ${result.stdout}  Process: example
  Should contain  ${result.stdout}  Generating test case 'startEvent__endEvent'

  # test cases written
  Should contain  ${result.stdout}  Writing test cases
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/generated/example/TC_startEvent__endEvent.java

  File should exist  ${testSources}/generated/example/TC_startEvent__endEvent.java

  # API classes written
  Should contain  ${result.stdout}  Writing API classes
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/ExternalTaskHandler.java
  Should contain  ${result.stdout}  Writing file: ${buildDir}/bpmndt/org/camunda/community/bpmndt/api/ExternalTaskClientHandler.java

  File should exist  ${testSources}/org/camunda/community/bpmndt/api/ExternalTaskHandler.java
  File should exist  ${testSources}/org/camunda/community/bpmndt/api/ExternalTaskClientHandler.java
