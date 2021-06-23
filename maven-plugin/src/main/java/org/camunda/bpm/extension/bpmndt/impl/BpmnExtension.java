package org.camunda.bpm.extension.bpmndt.impl;

import org.camunda.bpm.extension.bpmndt.impl.type.DescriptionImpl;
import org.camunda.bpm.extension.bpmndt.impl.type.NameImpl;
import org.camunda.bpm.extension.bpmndt.impl.type.PathImpl;
import org.camunda.bpm.extension.bpmndt.impl.type.PathNodeImpl;
import org.camunda.bpm.extension.bpmndt.impl.type.TestCaseImpl;
import org.camunda.bpm.extension.bpmndt.impl.type.TestCasesImpl;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.xml.ModelBuilder;

/**
 * Extended {@link Bpmn} instance, which registers additional types that are required to read the
 * {@code bpmndt} extension elements.
 */
class BpmnExtension extends Bpmn {

  @Override
  protected void doRegisterTypes(ModelBuilder modelBuilder) {
    super.doRegisterTypes(modelBuilder);

    TestCasesImpl.registerType(modelBuilder);
    TestCaseImpl.registerType(modelBuilder);
    DescriptionImpl.registerType(modelBuilder);
    NameImpl.registerType(modelBuilder);
    PathImpl.registerType(modelBuilder);
    PathNodeImpl.registerType(modelBuilder);
  }
}
