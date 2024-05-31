package org.camunda.community.bpmndt.model.element;

import static org.camunda.community.bpmndt.model.BpmnExtension.ELEMENT_NAME;
import static org.camunda.community.bpmndt.model.BpmnExtension.NS;

import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;

import io.camunda.zeebe.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;

/**
 * Test case name element.
 */
public class NameElement extends BpmnModelElementInstanceImpl {

  public static void registerType(ModelBuilder modelBuilder) {
    var typeBuilder = modelBuilder.defineType(NameElement.class, ELEMENT_NAME)
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
