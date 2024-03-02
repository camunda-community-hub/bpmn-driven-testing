package org.camunda.community.bpmndt.platform8.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.camunda.community.bpmndt.test.Platform8TestPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
public class AbstractTestCaseTest {

  @RegisterExtension
  TestCase tc = new TestCase();

  ZeebeTestEngine engine;

  @Test
  public void testExecute() {
    tc.createExecutor(engine)
        .verify(ProcessInstanceAssert::isCompleted)
        .execute();
  }

  private static class TestCase extends AbstractJUnit5TestCase {

    @Override
    public String getBpmnProcessId() {
      return "simple";
    }

    @Override
    public String getEnd() {
      return "endEvent";
    }

    @Override
    public String getStart() {
      return "startEvent";
    }

    @Override
    protected InputStream getBpmnResource() {
      try {
        return Files.newInputStream(Platform8TestPaths.simple("simple.bpmn"));
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    protected void execute(TestCaseInstance instance, ProcessInstanceEvent processInstanceEvent) {
      instance.hasPassed(processInstanceEvent, "startEvent");
      instance.hasPassed(processInstanceEvent, "endEvent");
      instance.isCompleted(processInstanceEvent);
    }
  }
}
