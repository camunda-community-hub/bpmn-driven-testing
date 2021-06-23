package org.camunda.bpm.extension.bpmndt.impl.type;

import static org.camunda.bpm.extension.bpmndt.Constants.ELEMENT_TEST_CASE;
import static org.camunda.bpm.extension.bpmndt.Constants.NS;

import java.util.List;

import org.camunda.bpm.extension.bpmndt.BpmnSupport;
import org.camunda.bpm.extension.bpmndt.type.Description;
import org.camunda.bpm.extension.bpmndt.type.Name;
import org.camunda.bpm.extension.bpmndt.type.Path;
import org.camunda.bpm.extension.bpmndt.type.TestCase;
import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

public class TestCaseImpl extends BpmnModelElementInstanceImpl implements TestCase {

  protected static ChildElement<Description> descriptionElement;
  protected static ChildElement<Name> nameElement;
  protected static ChildElement<Path> pathElement;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(TestCase.class, ELEMENT_TEST_CASE)
        .namespaceUri(NS)
        .instanceProvider(TestCaseImpl::new);

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    descriptionElement = sequenceBuilder.element(Description.class).build();
    nameElement = sequenceBuilder.element(Name.class).build();
    pathElement = sequenceBuilder.element(Path.class).build();

    typeBuilder.build();
  }

  private final Path path;

  public TestCaseImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);

    path = pathElement.getChild(this);
  }

  @Override
  public String getDescription() {
    Description description = descriptionElement.getChild(this);
    return description != null ? description.getValue() : null;
  }

  @Override
  public String getName() {
    Name name = nameElement.getChild(this);
    if (name != null) {
      return BpmnSupport.convert(name.getValue());
    }
    
    List<String> flowNodeIds = getPath().getFlowNodeIds();

    String a = BpmnSupport.convert(flowNodeIds.get(0));
    String b = BpmnSupport.convert(flowNodeIds.get(flowNodeIds.size() - 1));

    return String.format("%s__%s", a, b);
  }

  @Override
  public Path getPath() {
    return path;
  }
}
