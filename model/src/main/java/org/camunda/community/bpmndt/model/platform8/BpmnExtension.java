package org.camunda.community.bpmndt.model.platform8;

import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.community.bpmndt.model.element.DescriptionElement;
import org.camunda.community.bpmndt.model.element.NameElement;
import org.camunda.community.bpmndt.model.element.PathElement;
import org.camunda.community.bpmndt.model.element.PathNodeElement;
import org.camunda.community.bpmndt.model.element.TestCaseElement;
import org.camunda.community.bpmndt.model.element.TestCasesElement;

import io.camunda.zeebe.model.bpmn.Bpmn;

/**
 * Extended {@link Bpmn} instance, which registers additional types that are required to read the {@code bpmndt} extension elements.
 */
public class BpmnExtension extends Bpmn {

  /**
   * Reusable instance.
   */
  public static final BpmnExtension INSTANCE = new BpmnExtension();

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
