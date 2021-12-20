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
  public void testExtractActivityId() {
    assertThat(l.extractActivityId("test"), equalTo("test"));
    assertThat(l.extractActivityId("test#"), equalTo("test#"));
    assertThat(l.extractActivityId("test#multiInstance"), equalTo("test#multiInstance"));
    assertThat(l.extractActivityId("test#multiInstanceBody"), equalTo("test"));
  }
}
