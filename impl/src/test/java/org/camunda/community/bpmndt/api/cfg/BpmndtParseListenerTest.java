package org.camunda.community.bpmndt.api.cfg;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BpmndtParseListenerTest {

  private BpmndtParseListener l;

  @BeforeEach
  public void setUp() {
    l = new BpmndtParseListener();
  }

  @Test
  public void testStripMultiInstanceScopeSuffix() {
    assertThat(l.stripMultiInstanceScopeSuffix("test")).isEqualTo("test");
    assertThat(l.stripMultiInstanceScopeSuffix("test#")).isEqualTo("test#");
    assertThat(l.stripMultiInstanceScopeSuffix("test#multiInstance")).isEqualTo("test#multiInstance");
    assertThat(l.stripMultiInstanceScopeSuffix("test#multiInstanceBody")).isEqualTo("test");
  }
}
