package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.TC_simpleCollaboration__startEvent__endEvent;

public class SimpleCollaborationTest {

  @Rule
  public TC_simpleCollaboration__startEvent__endEvent tc = new TC_simpleCollaboration__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }
}
