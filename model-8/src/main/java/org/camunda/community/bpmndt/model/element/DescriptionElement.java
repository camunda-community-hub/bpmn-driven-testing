package org.camunda.community.bpmndt.model.element;

import static org.camunda.community.bpmndt.model.BpmnExtension.ELEMENT_DESCRIPTION;
import static org.camunda.community.bpmndt.model.BpmnExtension.NS;

import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;

import io.camunda.zeebe.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;

/**
 * Test case description element.
 */
public class DescriptionElement extends BpmnModelElementInstanceImpl {

  public static void registerType(ModelBuilder modelBuilder) {
    var typeBuilder = modelBuilder.defineType(DescriptionElement.class, ELEMENT_DESCRIPTION)
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
