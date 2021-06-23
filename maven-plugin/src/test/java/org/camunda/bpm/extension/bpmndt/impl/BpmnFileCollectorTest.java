package org.camunda.bpm.extension.bpmndt.impl;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.camunda.bpm.extension.bpmndt.Constants;
import org.junit.Test;

public class BpmnFileCollectorTest {

  @Test
  public void testCollect() {
    Collection<Path> bpmnFiles = new BpmnFileCollector(Paths.get("./src/test")).collect();
    assertThat(bpmnFiles.isEmpty(), is(false));

    for (Path bpmnFile : bpmnFiles) {
      assertThat(bpmnFile.getFileName().toString(), endsWith(Constants.BPMN_EXTENSION));
    }
  }
}
