package org.camunda.community.bpmndt.api;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

class TimerEventHandlerTest {

  @Test
  void testToDuration() {
    var handler = new TimerEventHandler("");

    assertThat(handler.toDuration(1000, 0).toMillis()).isEqualTo(1000);
    assertThat(handler.toDuration(1000, 1).toMillis()).isEqualTo(1000);
    assertThat(handler.toDuration(1000, 999).toMillis()).isEqualTo(1000);
    assertThat(handler.toDuration(1000, 1000).toMillis()).isEqualTo(0);

    assertThat(handler.toDuration(1710231187500L, 1710227587505L).toMillis()).isEqualTo(3600000);
    assertThat(handler.toDuration(1710231187500L, 1710227587500L).toMillis()).isEqualTo(3600000);
  }
}
