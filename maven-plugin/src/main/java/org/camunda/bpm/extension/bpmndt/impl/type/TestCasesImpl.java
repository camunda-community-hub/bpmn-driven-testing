package org.camunda.bpm.extension.bpmndt.impl.type;

import static org.camunda.bpm.extension.bpmndt.Constants.ELEMENT_TEST_CASES;
import static org.camunda.bpm.extension.bpmndt.Constants.NS;

import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.extension.bpmndt.type.TestCase;
import org.camunda.bpm.extension.bpmndt.type.TestCases;
import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;

/**
 * Custom extension element implementation.
 */
public class TestCasesImpl extends BpmnModelElementInstanceImpl implements TestCases {

  protected static ChildElementCollection<TestCase> testCaseCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(TestCases.class, ELEMENT_TEST_CASES)
        .namespaceUri(NS)
        .instanceProvider(TestCasesImpl::new);

    testCaseCollection = typeBuilder.sequence().elementCollection(TestCase.class).build();

    typeBuilder.build();
  }

  public TestCasesImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  @Override
  public List<TestCase> getTestCases() {
    return new LinkedList<>(testCaseCollection.get(this));
  }
}
