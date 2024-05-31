package org.camunda.community.bpmndt.model.element;

import static org.camunda.community.bpmndt.model.BpmnExtension.ELEMENT_TEST_CASES;
import static org.camunda.community.bpmndt.model.BpmnExtension.NS;

import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;

import io.camunda.zeebe.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;

/**
 * Custom BPMN extension element.
 */
public class TestCasesElement extends BpmnModelElementInstanceImpl {

  protected static ChildElementCollection<TestCaseElement> testCaseCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    var typeBuilder = modelBuilder.defineType(TestCasesElement.class, ELEMENT_TEST_CASES)
        .namespaceUri(NS)
        .instanceProvider(TestCasesElement::new);

    testCaseCollection = typeBuilder.sequence().elementCollection(TestCaseElement.class).build();

    typeBuilder.build();
  }

  public TestCasesElement(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  /**
   * Returns the defined test cases.
   *
   * @return A list of test cases.
   */
  public List<TestCaseElement> getTestCases() {
    return new LinkedList<>(testCaseCollection.get(this));
  }
}
