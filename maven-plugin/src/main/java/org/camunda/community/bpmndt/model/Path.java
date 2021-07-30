package org.camunda.community.bpmndt.model;

import static org.camunda.community.bpmndt.Constants.ELEMENT_PATH;
import static org.camunda.community.bpmndt.Constants.NS;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;

public class Path extends BpmnModelElementInstanceImpl {

  protected static ChildElementCollection<PathNode> pathNodeCollection;
  
  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Path.class, ELEMENT_PATH)
        .namespaceUri(NS)
        .instanceProvider(Path::new);

    pathNodeCollection = typeBuilder.sequence().elementCollection(PathNode.class).build();
    
    typeBuilder.build();
  }
  
  private final List<String> flowNodeIds;

  public Path(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);

    flowNodeIds = pathNodeCollection.get(this).stream().map(PathNode::getId).collect(Collectors.toList());
  }

  public List<String> getFlowNodeIds() {
    return flowNodeIds;
  }

  public int length() {
    return flowNodeIds.size();
  }
}
