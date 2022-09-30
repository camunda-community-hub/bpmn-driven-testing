package org.camunda.community.bpmndt.test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import com.squareup.javapoet.MethodSpec;

public class ContainsCode {

  public static ContainsCode containsCode(MethodSpec method) {
    return new ContainsCode(method);
  }

  private final MethodSpec method;
  private final String methodCode;

  private ContainsCode(MethodSpec method) {
    this.method = method;

    methodCode = method.code.toString();
  }

  /**
   * Asserts that the method's code contains the given substring.
   * 
   * @param substring The expected substring.
   * 
   * @return The current instance.
   */
  public ContainsCode contains(String substring) {
    assertThat(String.format("Method '%s'", method.name), methodCode, containsString(substring));
    return this;
  }

  /**
   * Asserts that the method's code does not contains the given substring.
   * 
   * @param substring The not expected substring.
   * 
   * @return The current instance.
   */
  public ContainsCode notContains(String substring) {
    assertThat(String.format("Method '%s'", method.name), methodCode, not(containsString(substring)));
    return this;
  }
}
