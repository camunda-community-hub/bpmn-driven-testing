package org.example.it;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.sequential.TC_startEvent__endEvent;

public class SequentialTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    assertThat(tc.handleMultiInstanceManualTask(), notNullValue());
    assertThat(tc.handleMultiInstanceManualTask().handle(0), nullValue());
    assertThat(tc.handleMultiInstanceManualTask().handle(), nullValue());

    tc.handleMultiInstanceManualTask().verifySequential();

    tc.createExecutor().execute();
  }
}
