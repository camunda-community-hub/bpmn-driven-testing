package org.example.it;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.externaltaskinputoutput.TC_startEvent__endEvent;

public class ExternalTaskInputOutputTest {

  @RegisterExtension
  public TC_startEvent__endEvent tc = new TC_startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.handleExternalTask().verifyTask((externalTaskAssert, localVariables) -> {
      externalTaskAssert.hasTopicName("test-topic");

      assertThat(localVariables.get("increment"), is(11L));
    });

    tc.createExecutor()
        .withVariable("i", 10L)
        .verify(pi -> {
          pi.isEnded();

          // verify output variables
          pi.variables().containsEntry("decrement", 10L);
        })
        .execute();
  }
}
