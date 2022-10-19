package org.camunda.community.bpmndt;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.community.bpmndt.model.Description;
import org.camunda.community.bpmndt.model.Name;
import org.camunda.community.bpmndt.model.Path;
import org.camunda.community.bpmndt.model.PathNode;
import org.camunda.community.bpmndt.model.TestCase;
import org.camunda.community.bpmndt.model.TestCases;

/**
 * Extended {@link Bpmn} instance, which registers additional types that are required to read the
 * {@code bpmndt} extension elements.
 */
class BpmnExtension extends Bpmn {

  @Override
  protected void doRegisterTypes(ModelBuilder modelBuilder) {
    super.doRegisterTypes(modelBuilder);

    TestCases.registerType(modelBuilder);
    TestCase.registerType(modelBuilder);
    Description.registerType(modelBuilder);
    Name.registerType(modelBuilder);
    Path.registerType(modelBuilder);
    PathNode.registerType(modelBuilder);
  }
}
