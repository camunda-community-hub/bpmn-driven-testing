package org.camunda.community.bpmndt.model;

import static org.camunda.community.bpmndt.Constants.ELEMENT_TEST_CASE;
import static org.camunda.community.bpmndt.Constants.NS;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * Test case definition.
 */
public class TestCase extends BpmnModelElementInstanceImpl {

  protected static ChildElement<Description> descriptionElement;
  protected static ChildElement<Name> nameElement;
  protected static ChildElement<Path> pathElement;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(TestCase.class, ELEMENT_TEST_CASE)
        .namespaceUri(NS)
        .instanceProvider(TestCase::new);

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    descriptionElement = sequenceBuilder.element(Description.class).build();
    nameElement = sequenceBuilder.element(Name.class).build();
    pathElement = sequenceBuilder.element(Path.class).build();

    typeBuilder.build();
  }

  private final Description description;
  private final Name name;
  private final Path path;

  public TestCase(ModelTypeInstanceContext instanceContext) {
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
  public Path getPath() {
    return path;
  }
}
