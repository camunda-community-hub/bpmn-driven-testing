package org.camunda.community.bpmndt.cmd.generation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import com.squareup.javapoet.ClassName;

public class GetProcessEnginePluginsTest {

  @Test
  public void testBuildClassName() {
    ClassName className = new GetProcessEnginePlugins().buildClassName("org.example.CustomPlugin");
    assertThat(className.packageName(), equalTo("org.example"));
    assertThat(className.simpleName(), equalTo("CustomPlugin"));
  }

  @Test
  public void testBuildClassNameNull() {
    assertThat(new GetProcessEnginePlugins().buildClassName("Xyz"), nullValue());
  }
}
