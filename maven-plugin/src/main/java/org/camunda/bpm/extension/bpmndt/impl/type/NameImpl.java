package org.camunda.bpm.extension.bpmndt.impl.type;

import static org.camunda.bpm.extension.bpmndt.Constants.ELEMENT_NAME;
import static org.camunda.bpm.extension.bpmndt.Constants.NS;

import org.camunda.bpm.extension.bpmndt.type.Name;
import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

public class NameImpl extends BpmnModelElementInstanceImpl implements Name {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Name.class, ELEMENT_NAME)
        .namespaceUri(NS)
        .instanceProvider(NameImpl::new);

    typeBuilder.build();
  }

  public NameImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  @Override
  public String getValue() {
    return getTextContent();
  }
}
