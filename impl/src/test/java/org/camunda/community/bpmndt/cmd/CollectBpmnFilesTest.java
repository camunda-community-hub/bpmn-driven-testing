package org.camunda.community.bpmndt.cmd;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import java.util.Collection;

import org.camunda.community.bpmndt.Constants;
import org.camunda.community.bpmndt.test.TestPaths;
import org.junit.jupiter.api.Test;

public class CollectBpmnFilesTest {

  @Test
  public void testCollect() {
    Collection<Path> bpmnFiles = new CollectBpmnFiles().apply(TestPaths.simple());
    assertThat(bpmnFiles.isEmpty()).isFalse();

    for (Path bpmnFile : bpmnFiles) {
      assertThat(bpmnFile.getFileName().toString()).endsWith(Constants.BPMN_EXTENSION);
    }
  }
}
