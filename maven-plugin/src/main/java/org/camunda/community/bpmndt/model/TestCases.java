package org.camunda.community.bpmndt.model;

import static org.camunda.community.bpmndt.Constants.ELEMENT_TEST_CASES;
import static org.camunda.community.bpmndt.Constants.NS;

import java.util.LinkedList;
import java.util.List;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;

/**
 * Custom extension element implementation.
 */
public class TestCases extends BpmnModelElementInstanceImpl {

  protected static ChildElementCollection<TestCase> testCaseCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(TestCases.class, ELEMENT_TEST_CASES)
        .namespaceUri(NS)
        .instanceProvider(TestCases::new);

    testCaseCollection = typeBuilder.sequence().elementCollection(TestCase.class).build();

    typeBuilder.build();
  }

  public TestCases(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  /**
   * Returns the defined test cases.
   * 
   * @return A list of test cases.
   */
  public List<TestCase> getTestCases() {
    return new LinkedList<>(testCaseCollection.get(this));
  }
}
