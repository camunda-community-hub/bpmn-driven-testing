package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.linkevent.TC_forkA__linkCatchEventA;
import generated.linkevent.TC_forkB__linkCatchEventB;

public class LinkEventTest {

  @Rule
  public TC_forkA__linkCatchEventA tcA = new TC_forkA__linkCatchEventA();
  @Rule
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
