package org.camunda.community.bpmndt.cmd;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import java.util.Collection;

import org.camunda.community.bpmndt.Constants;
import org.camunda.community.bpmndt.GeneratorContextBase;
import org.camunda.community.bpmndt.test.Platform7TestPaths;
import org.junit.jupiter.api.Test;

public class CollectBpmnFilesTest {

  @Test
  public void testCollect() {
    GeneratorContextBase ctx = new GeneratorContextBase();
    ctx.setMainResourcePath(Platform7TestPaths.simple());

    Collection<Path> bpmnFiles = new CollectBpmnFiles().apply(ctx);
    assertThat(bpmnFiles.isEmpty()).isFalse();

    for (Path bpmnFile : bpmnFiles) {
      assertThat(bpmnFile.getFileName().toString()).endsWith(Constants.BPMN_EXTENSION);
    }
  }
}
