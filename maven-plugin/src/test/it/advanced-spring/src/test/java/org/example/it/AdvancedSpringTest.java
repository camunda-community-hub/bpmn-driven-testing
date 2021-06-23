package org.example.it;

import org.springframework.test.context.ContextConfiguration;

import generated.TC_advancedSpring__startEvent__endEvent;

@ContextConfiguration(classes = TestConfiguration.class)
public class AdvancedSpringTest extends TC_advancedSpring__startEvent__endEvent {

  @Override
  protected void after() {
    assertThatPi().isEnded();
  }
}
