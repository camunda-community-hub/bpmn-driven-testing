package org.camunda.community.bpmndt.model;

import static org.camunda.community.bpmndt.Constants.ELEMENT_NAME;
import static org.camunda.community.bpmndt.Constants.NS;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

/**
 * Test case name.
 */
public class Name extends BpmnModelElementInstanceImpl {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Name.class, ELEMENT_NAME)
        .namespaceUri(NS)
        .instanceProvider(Name::new);

    typeBuilder.build();
  }

  public Name(ModelTypeInstanceContext instanceContext) {
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
