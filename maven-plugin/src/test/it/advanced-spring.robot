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

  Should be equal as integers  ${result.rc}  0
