package org.camunda.community.bpmndt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.camunda.bpm.model.bpmn.instance.ThrowEvent;
import org.junit.Before;
import org.junit.Test;

public class BpmnEventSupportTest {

  private BpmnEventSupport bpmnEventSupport;
  private BpmnSupport bpmnSupport;

  private Path advancedMultiInstance;

  @Before
  public void setUp() {
    advancedMultiInstance = Paths.get("./src/test/it/advanced-multi-instance/src/main/resources");
  }

  @Test
  public void testIsNoneEndEvent() {
    bpmnSupport = BpmnSupport.of(advancedMultiInstance.resolve("scopeSequential.bpmn"));
    bpmnEventSupport = new BpmnEventSupport((ThrowEvent) bpmnSupport.get("subProcessEndEvent"));
    assertThat(bpmnEventSupport.isNoneEnd(), is(true));

    bpmnSupport = BpmnSupport.of(advancedMultiInstance.resolve("scopeErrorEndEvent.bpmn"));
    bpmnEventSupport = new BpmnEventSupport((ThrowEvent) bpmnSupport.get("subProcessErrorEndEvent"));
    assertThat(bpmnEventSupport.isNoneEnd(), is(false));
  }
}
