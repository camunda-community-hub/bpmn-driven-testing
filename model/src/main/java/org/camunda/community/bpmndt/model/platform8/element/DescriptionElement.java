package org.camunda.community.bpmndt.model.platform8.element;

import static org.camunda.community.bpmndt.model.Constants.ELEMENT_DESCRIPTION;
import static org.camunda.community.bpmndt.model.Constants.NS;

import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

import io.camunda.zeebe.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;

/**
 * Test case description element.
 */
public class DescriptionElement extends BpmnModelElementInstanceImpl {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(DescriptionElement.class, ELEMENT_DESCRIPTION)
        .namespaceUri(NS)
        .instanceProvider(DescriptionElement::new);

    typeBuilder.build();
  }

  public DescriptionElement(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  /**
   * Returns the description as text value.
   *
   * @return The value.
   */
  public String getValue() {
    return getTextContent();
  }
}
