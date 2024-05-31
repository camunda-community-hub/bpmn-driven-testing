package org.camunda.community.bpmndt.model.element;

import static org.camunda.community.bpmndt.model.BpmnExtension.ELEMENT_PATH;
import static org.camunda.community.bpmndt.model.BpmnExtension.NS;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;

import io.camunda.zeebe.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;

/**
 * Path of a test case.
 */
public class PathElement extends BpmnModelElementInstanceImpl {

  protected static ChildElementCollection<PathNodeElement> nodeCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    var typeBuilder = modelBuilder.defineType(PathElement.class, ELEMENT_PATH)
        .namespaceUri(NS)
        .instanceProvider(PathElement::new);

    nodeCollection = typeBuilder.sequence().elementCollection(PathNodeElement.class).build();

    typeBuilder.build();
  }

  private final List<String> flowNodeIds;

  public PathElement(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);

    Collection<PathNodeElement> nodes = nodeCollection.get(this);
    if (nodes == null) {
      flowNodeIds = Collections.emptyList();
    } else {
      flowNodeIds = nodes.stream().map(PathNodeElement::getId).filter(Objects::nonNull).collect(Collectors.toList());
    }
  }

  /**
   * Returns a list of flow node IDs, that define the test case's path.
   *
   * @return A list of flow node IDs - can be empty.
   */
  public List<String> getFlowNodeIds() {
    return flowNodeIds;
  }
}
