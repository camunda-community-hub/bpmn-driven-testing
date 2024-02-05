package org.camunda.community.bpmndt.model.platform8;

import org.camunda.community.bpmndt.model.platform8.element.DescriptionElement;
import org.camunda.community.bpmndt.model.platform8.element.NameElement;
import org.camunda.community.bpmndt.model.platform8.element.PathElement;
import org.camunda.community.bpmndt.model.platform8.element.PathNodeElement;
import org.camunda.community.bpmndt.model.platform8.element.TestCaseElement;
import org.camunda.community.bpmndt.model.platform8.element.TestCasesElement;

import io.camunda.zeebe.model.bpmn.Bpmn;

/**
 * Customizes the {@link Bpmn} instance by registering additional types that are required to read the {@code bpmndt} extension elements.
 */
public class BpmnExtension extends Bpmn {

  public static void registerTypes() {
    var modelBuilder = Bpmn.INSTANCE.getBpmnModelBuilder();

    TestCasesElement.registerType(modelBuilder);
    TestCaseElement.registerType(modelBuilder);
    DescriptionElement.registerType(modelBuilder);
    NameElement.registerType(modelBuilder);
    PathElement.registerType(modelBuilder);
    PathNodeElement.registerType(modelBuilder);

    Bpmn.INSTANCE.setBpmnModel(modelBuilder.build());
  }
}
