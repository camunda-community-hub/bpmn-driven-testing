package org.camunda.community.bpmndt.model;

import static org.camunda.community.bpmndt.Constants.ELEMENT_DESCRIPTION;
import static org.camunda.community.bpmndt.Constants.NS;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

/**
 * Test case description.
 */
public class Description extends BpmnModelElementInstanceImpl {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Description.class, ELEMENT_DESCRIPTION)
        .namespaceUri(NS)
        .instanceProvider(Description::new);

    typeBuilder.build();
  }

  public Description(ModelTypeInstanceContext instanceContext) {
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
