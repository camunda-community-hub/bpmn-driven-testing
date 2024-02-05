package org.example.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.linkevent.TC_forkA__linkCatchEventA;
import generated.linkevent.TC_forkB__linkCatchEventB;

public class LinkEventTest {

  @RegisterExtension
  public TC_forkA__linkCatchEventA tcA = new TC_forkA__linkCatchEventA();
  @RegisterExtension
  public TC_forkB__linkCatchEventB tcB = new TC_forkB__linkCatchEventB();

  @Test
  public void testExecuteA() {
    tcA.createExecutor().withVariable("forkA", true).execute();
  }

  @Test
  public void testExecuteB() {
    tcB.createExecutor().withVariable("forkB", true).execute();
  }
}
