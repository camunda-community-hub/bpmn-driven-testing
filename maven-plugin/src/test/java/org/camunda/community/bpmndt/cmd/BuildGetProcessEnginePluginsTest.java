package org.camunda.community.bpmndt.cmd;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import com.squareup.javapoet.ClassName;

public class BuildGetProcessEnginePluginsTest {

  @Test
  public void testBuildClassName() {
    ClassName className = new BuildGetProcessEnginePlugins().buildClassName("org.example.CustomPlugin");
    assertThat(className.packageName(), equalTo("org.example"));
    assertThat(className.simpleName(), equalTo("CustomPlugin"));
  }

  @Test
  public void testBuildClassNameNull() {
    assertThat(new BuildGetProcessEnginePlugins().buildClassName("Xyz"), nullValue());
  }
}
