package org.camunda.community.bpmndt.api.cfg;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class BpmndtParseListenerTest {

  private BpmndtParseListener l;

  @Before
  public void setUp() {
    l = new BpmndtParseListener();
  }

  @Test
  public void testStripMultiInstanceScopeSuffix() {
    assertThat(l.stripMultiInstanceScopeSuffix("test"), equalTo("test"));
    assertThat(l.stripMultiInstanceScopeSuffix("test#"), equalTo("test#"));
    assertThat(l.stripMultiInstanceScopeSuffix("test#multiInstance"), equalTo("test#multiInstance"));
    assertThat(l.stripMultiInstanceScopeSuffix("test#multiInstanceBody"), equalTo("test"));
  }
}
