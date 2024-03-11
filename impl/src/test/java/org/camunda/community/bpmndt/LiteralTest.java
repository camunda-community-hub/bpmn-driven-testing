package org.camunda.community.bpmndt;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

public class LiteralTest {

  @Test
  public void testToJavaLiteral() {
    assertThat(Literal.toJavaLiteral("Happy Path")).isEqualTo("happy_path");
    assertThat(Literal.toJavaLiteral("Happy-Path")).isEqualTo("happy_path");
    assertThat(Literal.toJavaLiteral("Happy Path!")).isEqualTo("happy_path_");
    assertThat(Literal.toJavaLiteral("startEvent__endEvent")).isEqualTo("startevent__endevent");
    assertThat(Literal.toJavaLiteral("123\nABC")).isEqualTo("_123_abc");
    assertThat(Literal.toJavaLiteral("New")).isEqualTo("_new");
  }

  @Test
  public void testToLiteral() {
    assertThat(Literal.toLiteral("Happy Path")).isEqualTo("Happy_Path");
    assertThat(Literal.toLiteral("Happy-Path")).isEqualTo("Happy_Path");
    assertThat(Literal.toLiteral("Happy Path!")).isEqualTo("Happy_Path_");
    assertThat(Literal.toLiteral("startEvent__endEvent")).isEqualTo("startEvent__endEvent");
    assertThat(Literal.toLiteral("123\nABC")).isEqualTo("123_ABC");
  }
}
