package org.camunda.community.bpmndt.model.platform8.element;

import static org.camunda.community.bpmndt.model.Constants.ELEMENT_NODE;
import static org.camunda.community.bpmndt.model.Constants.NS;

import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

import io.camunda.zeebe.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;

/**
 * Node of a path, that is represented by the ID of the related flow node.
 */
public class PathNodeElement extends BpmnModelElementInstanceImpl {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(PathNodeElement.class, ELEMENT_NODE)
        .namespaceUri(NS)
        .instanceProvider(PathNodeElement::new);

    typeBuilder.build();
  }

  public PathNodeElement(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  /**
   * Returns the flow node ID of the test path node.
   *
   * @return The flow node ID.
   */
  public String getId() {
    return getTextContent();
  }
}
