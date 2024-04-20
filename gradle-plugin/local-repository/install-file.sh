#!/bin/bash

artifact_id="$1"

if [[ $artifact_id == plugins/* ]]; then
  sub_directory="lib/plugins"
else
  sub_directory="lib"
fi

artifact_id="${artifact_id#plugins/}"

mvn install:install-file \
-DgroupId=org.gradle \
-DartifactId=${artifact_id} \
-Dversion=${GRADLE_VERSION} \
-Dpackaging=jar \
-Dfile=${GRADLE_HOME}/${sub_directory}/${artifact_id}-${GRADLE_VERSION}.jar \
-DcreateChecksum=true \
-DlocalRepositoryPath=.
