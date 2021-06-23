package org.camunda.bpm.extension.bpmndt.impl.type;

import static org.camunda.bpm.extension.bpmndt.Constants.ELEMENT_PATH;
import static org.camunda.bpm.extension.bpmndt.Constants.NS;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.extension.bpmndt.type.Path;
import org.camunda.bpm.extension.bpmndt.type.PathNode;
import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;

public class PathImpl extends BpmnModelElementInstanceImpl implements Path {

  protected static ChildElementCollection<PathNode> pathNodeCollection;
  
  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Path.class, ELEMENT_PATH)
        .namespaceUri(NS)
        .instanceProvider(PathImpl::new);

    pathNodeCollection = typeBuilder.sequence().elementCollection(PathNode.class).build();
    
    typeBuilder.build();
  }
  
  private final List<String> flowNodeIds;

  public PathImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);

    flowNodeIds = pathNodeCollection.get(this).stream().map(PathNode::getId).collect(Collectors.toList());
  }

  @Override
  public String getEnd() {
    return flowNodeIds.size() < 2 ? null : flowNodeIds.get(flowNodeIds.size() - 1);
  }

  @Override
  public List<String> getFlowNodeIds() {
    return flowNodeIds;
  }

  @Override
  public String getStart() {
    return flowNodeIds.size() < 2 ? null : flowNodeIds.get(0);
  }
}
