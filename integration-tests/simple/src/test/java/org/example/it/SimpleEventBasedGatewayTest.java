package org.example.it;

import org.junit.Rule;
import org.junit.Test;

import generated.simpleeventbasedgateway.TC_Message;
import generated.simpleeventbasedgateway.TC_Timer;
import generated.simpleeventbasedgateway.TC_startEvent__eventBasedGateway;

public class SimpleEventBasedGatewayTest {

  @Rule
  public TC_startEvent__eventBasedGateway tc = new TC_startEvent__eventBasedGateway();
  @Rule
  public TC_Message tcMessage = new TC_Message();
  @Rule
  public TC_Timer tcTimer = new TC_Timer();

  @Test
  public void testExecute() {
    tc.createExecutor().execute();
  }

  @Test
  public void testExecuteMessage() {
    tcMessage.createExecutor().execute();
  }

  @Test
  public void testExecuteTimer() {
    tcTimer.createExecutor().execute();
  }
}
