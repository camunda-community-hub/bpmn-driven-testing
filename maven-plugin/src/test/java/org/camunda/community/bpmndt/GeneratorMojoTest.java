package org.camunda.community.bpmndt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.maven.artifact.Artifact;
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
    Artifact h2 = Mockito.mock(Artifact.class);
    when(h2.getGroupId()).thenReturn(GeneratorMojo.H2_GROUP_ID);
    when(h2.getArtifactId()).thenReturn("no-h2");

    when(mojo.project.getDependencyArtifacts()).thenReturn(Collections.singleton(h2));

    assertThat(mojo.isH2Version2(), is(false));
  }

  @Test
  public void testIsH2Version2_V1() {
    Artifact h2 = Mockito.mock(Artifact.class);
    when(h2.getGroupId()).thenReturn(GeneratorMojo.H2_GROUP_ID);
    when(h2.getArtifactId()).thenReturn(GeneratorMojo.H2_ARTIFACT_ID);
    when(h2.getVersion()).thenReturn("1.4.197");

    when(mojo.project.getDependencyArtifacts()).thenReturn(Collections.singleton(h2));

    assertThat(mojo.isH2Version2(), is(false));
  }

  @Test
  public void testIsH2Version2_V2() {
    Artifact h2 = Mockito.mock(Artifact.class);
    when(h2.getGroupId()).thenReturn(GeneratorMojo.H2_GROUP_ID);
    when(h2.getArtifactId()).thenReturn(GeneratorMojo.H2_ARTIFACT_ID);
    when(h2.getVersion()).thenReturn("2.0.206");

    when(mojo.project.getDependencyArtifacts()).thenReturn(Collections.singleton(h2));

    assertThat(mojo.isH2Version2(), is(true));
  }
}
