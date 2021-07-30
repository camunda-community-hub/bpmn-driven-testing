package org.camunda.community.bpmndt.cmd;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.camunda.community.bpmndt.Constants;
import org.junit.Test;

public class CollectBpmnFilesTest {

  @Test
  public void testCollect() {
    Collection<Path> bpmnFiles = new CollectBpmnFiles().apply(Paths.get("./src/test"));
    assertThat(bpmnFiles.isEmpty(), is(false));

    for (Path bpmnFile : bpmnFiles) {
      assertThat(bpmnFile.getFileName().toString(), endsWith(Constants.BPMN_EXTENSION));
    }
  }
}
