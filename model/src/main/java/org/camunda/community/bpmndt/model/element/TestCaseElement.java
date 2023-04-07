package org.camunda.community.bpmndt.model.element;

import static org.camunda.community.bpmndt.model.BpmnExtension.ELEMENT_TEST_CASE;
import static org.camunda.community.bpmndt.model.BpmnExtension.NS;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * Test case element.
 */
public class TestCaseElement extends BpmnModelElementInstanceImpl {

  protected static ChildElement<DescriptionElement> descriptionElement;
  protected static ChildElement<NameElement> nameElement;
  protected static ChildElement<PathElement> pathElement;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(TestCaseElement.class, ELEMENT_TEST_CASE)
        .namespaceUri(NS)
        .instanceProvider(TestCaseElement::new);

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    descriptionElement = sequenceBuilder.element(DescriptionElement.class).build();
    nameElement = sequenceBuilder.element(NameElement.class).build();
    pathElement = sequenceBuilder.element(PathElement.class).build();

    typeBuilder.build();
  }

  private final DescriptionElement description;
  private final NameElement name;
  private final PathElement path;

  public TestCaseElement(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);

    description = descriptionElement.getChild(this);
    name = nameElement.getChild(this);
    path = pathElement.getChild(this);
  }

  /**
   * Returns the test case's description.
   * 
   * @return The description or {@code null}, if not specified.
   */
  public String getDescription() {
    return description != null ? description.getValue() : null;
  }

  /**
   * Returns the test case's name.
   * 
   * @return The name or {@code null}, if not specified.
   */
  public String getName() {
    return name != null ? name.getValue() : null;
  }

  /**
   * Provides the path that should be tested.
   * 
   * @return The test path.
   */
  public PathElement getPath() {
    return path;
  }
}
