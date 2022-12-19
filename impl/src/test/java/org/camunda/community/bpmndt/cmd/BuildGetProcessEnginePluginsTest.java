package org.camunda.community.bpmndt.cmd;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

import com.squareup.javapoet.ClassName;

public class BuildGetProcessEnginePluginsTest {

  @Test
  public void testBuildClassName() {
    ClassName className = new BuildGetProcessEnginePlugins().buildClassName("org.example.CustomPlugin");
    assertThat(className.packageName()).isEqualTo("org.example");
    assertThat(className.simpleName()).isEqualTo("CustomPlugin");
  }

  @Test
  public void testBuildClassNameNull() {
    assertThat(new BuildGetProcessEnginePlugins().buildClassName("Xyz")).isNull();
  }
}
