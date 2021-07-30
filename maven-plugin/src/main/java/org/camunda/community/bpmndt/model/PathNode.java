package org.camunda.community.bpmndt.model;

import static org.camunda.community.bpmndt.Constants.ELEMENT_NODE;
import static org.camunda.community.bpmndt.Constants.NS;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

/**
 * Node of a path, that is represented by the ID of the related flow node.
 */
public class PathNode extends BpmnModelElementInstanceImpl {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(PathNode.class, ELEMENT_NODE)
        .namespaceUri(NS)
        .instanceProvider(PathNode::new);

    typeBuilder.build();
  }

  public PathNode(ModelTypeInstanceContext instanceContext) {
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
