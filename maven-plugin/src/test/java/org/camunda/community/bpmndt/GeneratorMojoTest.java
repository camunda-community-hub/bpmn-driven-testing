package org.camunda.community.bpmndt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class GeneratorMojoTest {

  private GeneratorMojo mojo;

  @Before
  public void setUp() {
    mojo = new GeneratorMojo();
    mojo.project = Mockito.mock(MavenProject.class);
  }

  @Test
  public void testIsH2Version2_NoDependencies() {
    assertThat(mojo.isH2Version2(), is(false));
  }

  @Test
  public void testIsH2Version2_NoH2Dependency() {
    Dependency dependency = new Dependency();
    dependency.setGroupId(GeneratorMojo.H2_GROUP_ID);
    dependency.setArtifactId("no-h2");

    when(mojo.project.getDependencies()).thenReturn(Collections.singletonList(dependency));

    assertThat(mojo.isH2Version2(), is(false));
  }

  @Test
  public void testIsH2Version2_V1() {
    Dependency dependency = new Dependency();
    dependency.setGroupId(GeneratorMojo.H2_GROUP_ID);
    dependency.setArtifactId(GeneratorMojo.H2_ARTIFACT_ID);
    dependency.setVersion("1.4.197");

    when(mojo.project.getDependencies()).thenReturn(Collections.singletonList(dependency));

    assertThat(mojo.isH2Version2(), is(false));
  }

  @Test
  public void testIsH2Version2_V2() {
    Dependency dependency = new Dependency();
    dependency.setGroupId(GeneratorMojo.H2_GROUP_ID);
    dependency.setArtifactId(GeneratorMojo.H2_ARTIFACT_ID);
    dependency.setVersion("2.0.206");

    when(mojo.project.getDependencies()).thenReturn(Collections.singletonList(dependency));

    assertThat(mojo.isH2Version2(), is(true));
  }
}
