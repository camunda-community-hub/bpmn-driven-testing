package org.camunda.community.bpmndt.model;

import java.io.InputStream;

import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.ModelImpl;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.community.bpmndt.model.element.DescriptionElement;
import org.camunda.community.bpmndt.model.element.NameElement;
import org.camunda.community.bpmndt.model.element.PathElement;
import org.camunda.community.bpmndt.model.element.PathNodeElement;
import org.camunda.community.bpmndt.model.element.TestCaseElement;
import org.camunda.community.bpmndt.model.element.TestCasesElement;

import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.impl.BpmnImpl;
import io.camunda.zeebe.model.bpmn.impl.BpmnModelInstanceImpl;
import io.camunda.zeebe.model.bpmn.impl.BpmnParser;

/**
 * Customizes the {@link Bpmn} instance by registering additional types that are required to read the {@code bpmndt} extension elements.
 */
public final class BpmnExtension {

  public static final String ELEMENT_DESCRIPTION = "description";
  public static final String ELEMENT_NAME = "name";
  public static final String ELEMENT_NODE = "node";
  public static final String ELEMENT_PATH = "path";
  public static final String ELEMENT_TEST_CASE = "testCase";
  public static final String ELEMENT_TEST_CASES = "testCases";

  public static final String MODELER_EXECUTION_PLATFORM = "Camunda Cloud";
  public static final String MODELER_EXECUTION_PLATFORM_ATTRIBUTE = "executionPlatform";
  public static final String MODELER_NS = "http://camunda.org/schema/modeler/1.0";
  public static final String ZEEBE_MODELER_TEMPLATE_ATTRIBUTE = "modelerTemplate";
  public static final String ZEEBE_NS = "http://camunda.org/schema/zeebe/1.0";

  public static final String NS = "http://camunda.org/schema/extension/bpmn-driven-testing";

  private static final CustomBpmn INSTANCE = new CustomBpmn();
  private static final CustomBpmnParser PARSER_INSTANCE = new CustomBpmnParser();

  public static BpmnModelInstance readModelFromStream(InputStream stream) {
    return PARSER_INSTANCE.parseModelFromStream(stream);
  }

  private BpmnExtension() {
  }

  private static class CustomBpmn extends BpmnImpl {

    @Override
    protected void doRegisterTypes(ModelBuilder modelBuilder) {
      super.doRegisterTypes(modelBuilder);

      TestCasesElement.registerType(modelBuilder);
      TestCaseElement.registerType(modelBuilder);
      DescriptionElement.registerType(modelBuilder);
      NameElement.registerType(modelBuilder);
      PathElement.registerType(modelBuilder);
      PathNodeElement.registerType(modelBuilder);
    }
  }

  private static class CustomBpmnParser extends BpmnParser {

    @Override
    protected BpmnModelInstanceImpl createModelInstance(final DomDocument document) {
      return new BpmnModelInstanceImpl((ModelImpl) INSTANCE.getBpmnModel(), INSTANCE.getBpmnModelBuilder(), document);
    }
  }
}
