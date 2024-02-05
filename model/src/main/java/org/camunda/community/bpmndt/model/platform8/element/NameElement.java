package org.camunda.community.bpmndt.model.platform8.element;

import static org.camunda.community.bpmndt.model.Constants.ELEMENT_NAME;
import static org.camunda.community.bpmndt.model.Constants.NS;

import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

import io.camunda.zeebe.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;

/**
 * Test case name element.
 */
public class NameElement extends BpmnModelElementInstanceImpl {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(NameElement.class, ELEMENT_NAME)
        .namespaceUri(NS)
        .instanceProvider(NameElement::new);

    typeBuilder.build();
  }

  public NameElement(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  /**
   * Returns the name as text value.
   *
   * @return The value.
   */
  public String getValue() {
    return getTextContent();
  }
}
