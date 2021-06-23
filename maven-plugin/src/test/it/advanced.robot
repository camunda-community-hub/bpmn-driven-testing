* Settings
Library  OperatingSystem
Library  Process

* Test Cases
mvn clean test
  [Tags]  xignore
  ${result}=  Run process
  ...  mvn  -B  -f  ${CURDIR}/advanced/pom.xml  clean  package  -Dplugin.version\=${VERSION}
  ...  shell=True  stdout=${TEMP}/advanced.out  stderr=STDOUT

  Log  ${result.stdout}

  # plugin executed
  Should contain  ${result.stdout}  bpmn-driven-testing-maven-plugin:${VERSION}:generator

  # tests executed
  Should contain  ${result.stdout}  Running org.example.it.CallActivityNoMappingTest
  Should contain  ${result.stdout}  Running org.example.it.CallActivityTest
  # tests executed successfully
  Should contain  ${result.stdout}  Failures: 0, Errors: 0, Skipped: 0

  Should be equal as integers  ${result.rc}  0
