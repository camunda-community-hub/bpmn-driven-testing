### Local repository with required Gradle dependencies

The following commands are used to install the artifacts into the local repository:

```sh
export GRADLE_HOME="<path to gradle installation>"
export GRADLE_VERSION=8.5

bash install-file.sh gradle-base-services
bash install-file.sh gradle-core
bash install-file.sh gradle-core-api
bash install-file.sh gradle-model-core

bash install-file.sh plugins/gradle-platform-jvm
bash install-file.sh plugins/gradle-plugins
bash install-file.sh plugins/gradle-plugins-java
bash install-file.sh plugins/gradle-plugins-java-base
```
