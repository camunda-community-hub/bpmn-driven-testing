package org.camunda.bpm.extension.bpmndt.impl.type;

import static org.camunda.bpm.extension.bpmndt.Constants.ELEMENT_DESCRIPTION;
import static org.camunda.bpm.extension.bpmndt.Constants.NS;

import org.camunda.bpm.extension.bpmndt.type.Description;
import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

public class DescriptionImpl extends BpmnModelElementInstanceImpl implements Description {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Description.class, ELEMENT_DESCRIPTION)
        .namespaceUri(NS)
        .instanceProvider(DescriptionImpl::new);

    typeBuilder.build();
  }

  public DescriptionImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  @Override
  public String getValue() {
    return getTextContent();
  }
}
