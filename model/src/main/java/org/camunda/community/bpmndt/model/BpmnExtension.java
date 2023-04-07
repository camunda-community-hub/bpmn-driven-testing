package org.camunda.community.bpmndt.model;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.community.bpmndt.model.element.DescriptionElement;
import org.camunda.community.bpmndt.model.element.NameElement;
import org.camunda.community.bpmndt.model.element.PathElement;
import org.camunda.community.bpmndt.model.element.PathNodeElement;
import org.camunda.community.bpmndt.model.element.TestCaseElement;
import org.camunda.community.bpmndt.model.element.TestCasesElement;

/**
 * Extended {@link Bpmn} instance, which registers additional types that are required to read the
 * {@code bpmndt} extension elements.
 */
public class BpmnExtension extends Bpmn {

  /** Reusable instance. */
  public static final BpmnExtension INSTANCE = new BpmnExtension();

  public static final String ELEMENT_DESCRIPTION = "description";
  public static final String ELEMENT_NAME = "name";
  public static final String ELEMENT_NODE = "node";
  public static final String ELEMENT_PATH = "path";
  public static final String ELEMENT_TEST_CASE = "testCase";
  public static final String ELEMENT_TEST_CASES = "testCases";

  public static final String NS = "http://camunda.org/schema/extension/bpmn-driven-testing";

  @Override
  protected void doRegisterTypes(ModelBuilder modelBuilder) {
    super.doRegisterTypes(modelBuilder);

    TestCasesElement.registerType(modelBuilder);
    TestCaseElement.registerType(modelBuilder);
    DescriptionElement.registerType(modelBuilder);
    NameElement.registerType(modelBuilder);
    PathElement.registerType(modelBuilder);
    PathNodeElement.registerType(modelBuilder);
  }
}
