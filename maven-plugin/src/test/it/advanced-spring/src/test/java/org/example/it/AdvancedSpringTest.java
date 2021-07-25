package org.example.it;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import generated.BpmndtConfiguration;
import generated.TC_advancedSpring__startEvent__endEvent;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BpmndtConfiguration.class, TestConfiguration.class})
public class AdvancedSpringTest {

  @Rule
  public TC_advancedSpring__startEvent__endEvent tc = new TC_advancedSpring__startEvent__endEvent();

  @Test
  public void testExecute() {
    tc.createExecutor().verify(pi -> {
      pi.isEnded();
    }).execute();
  }
}
