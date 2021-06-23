package org.camunda.bpm.extension.bpmndt.impl.type;

import static org.camunda.bpm.extension.bpmndt.Constants.ELEMENT_NODE;
import static org.camunda.bpm.extension.bpmndt.Constants.NS;

import org.camunda.bpm.extension.bpmndt.type.PathNode;
import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

public class PathNodeImpl extends BpmnModelElementInstanceImpl implements PathNode {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(PathNode.class, ELEMENT_NODE)
        .namespaceUri(NS)
        .instanceProvider(PathNodeImpl::new);

    typeBuilder.build();
  }

  public PathNodeImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  @Override
  public String getId() {
    return getTextContent();
  }
}
