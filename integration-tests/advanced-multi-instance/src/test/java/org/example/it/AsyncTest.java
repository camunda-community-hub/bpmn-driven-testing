package org.example.it;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.async.TC_startEvent__endEvent;

public class AsyncTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    assertThat(tc.handleMultiInstanceManualTaskBefore(), notNullValue());
    assertThat(tc.handleMultiInstanceManualTaskAfter(), notNullValue());

    tc.createExecutor().execute();
  }
}
