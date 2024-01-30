package org.camunda.community.bpmndt.model.platform8;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.camunda.bpm.model.xml.ModelParseException;
import org.camunda.community.bpmndt.model.element.TestCaseElement;
import org.camunda.community.bpmndt.model.element.TestCasesElement;

import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.BoundaryEvent;
import io.camunda.zeebe.model.bpmn.instance.IntermediateCatchEvent;
import io.camunda.zeebe.model.bpmn.instance.IntermediateThrowEvent;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.ReceiveTask;

class TestCasesImpl implements TestCases {

  static TestCasesImpl of(InputStream stream) {
    try {
      return of(BpmnExtension.readModelFromStream(stream));
    } catch (ModelParseException e) {
      throw new RuntimeException("BPMN model could not be parsed", e);
    }
  }

  static TestCasesImpl of(BpmnModelInstance modelInstance) {
    return new TestCasesImpl(modelInstance).map();
  }

  static TestCasesImpl of(Path bpmnFile) {
    try (FileInputStream fis = new FileInputStream(bpmnFile.toFile())) {
      return of(fis);
    } catch (FileNotFoundException e) {
      throw new RuntimeException("BPMN file could not be found", e);
    } catch (IOException e) {
      throw new RuntimeException("BPMN file could not be read", e);
    }
  }

  final BpmnModelInstance modelInstance;

  private final Set<String> processIds;
  private final List<TestCase> testCases;

  private BpmnSupport bpmnSupport;

  TestCasesImpl(BpmnModelInstance modelInstance) {
    this.modelInstance = modelInstance;

    processIds = new TreeSet<>();
    testCases = new LinkedList<>();
  }

  @Override
  public List<TestCase> get() {
    return testCases;
  }

  @Override
  public List<TestCase> get(String processId) {
    return testCases.stream().filter(testCase -> testCase.getProcessId().equals(processId)).collect(Collectors.toList());
  }

  @Override
  public BpmnModelInstance getModelInstance() {
    return modelInstance;
  }

  @Override
  public Set<String> getProcessIds() {
    return processIds;
  }

  @Override
  public boolean isEmpty() {
    return testCases.isEmpty();
  }

  List<TestCaseElement> getTestCaseElements(Process process) {
    if (process.getExtensionElements() == null) {
      return Collections.emptyList();
    }

    var testCasesElement = (TestCasesElement) process.getExtensionElements().getUniqueChildElementByType(TestCasesElement.class);
    if (testCasesElement == null) {
      return Collections.emptyList();
    }

    return testCasesElement.getTestCases();
  }

  private void addElement(TestCaseImpl testCase, BpmnElementImpl next) {
    testCase.addElement(next);

    var scope = addScope(testCase, next.getId());
    if (scope != null) {
      scope.addElement(next);
    }
  }

  /**
   * Adds the BPMN element's parent as scope and returns it.
   *
   * @param testCase  The current test case.
   * @param elementId The current element ID.
   * @return The parent scope or {@code null}, if the parent is the process.
   */
  private BpmnElementScopeImpl addScope(TestCaseImpl testCase, String elementId) {
    var parentElementId = bpmnSupport.getParentElementId(elementId);
    if (testCase.getProcessId().equals(parentElementId)) {
      return null;
    }

    var scope = testCase.getScope(parentElementId);
    if (scope == null) {
      scope = new BpmnElementScopeImpl();
      scope.flowNode = bpmnSupport.get(parentElementId);
      scope.multiInstanceLoopCharacteristics = bpmnSupport.getMultiInstanceLoopCharacteristics(parentElementId);

      testCase.addScope(scope);

      scope.parent = addScope(testCase, scope.getId());
    }

    return scope;
  }

  private void handleBoundaryEvent(BpmnElementImpl element) {
    var event = element.getFlowNode(BoundaryEvent.class);
    var eventSupport = new BpmnEventSupport(event);

    element.attachedTo = event.getAttachedTo().getId();

    if (eventSupport.isError()) {
      var error = eventSupport.getError();

      element.type = BpmnElementType.ERROR_BOUNDARY;
      element.eventCode = error != null ? error.getErrorCode() : null;
    } else if (eventSupport.isEscalation()) {
      var escalation = eventSupport.getEscalation();

      element.type = BpmnElementType.ESCALATION_BOUNDARY;
      element.eventCode = escalation != null ? escalation.getEscalationCode() : null;
    } else if (eventSupport.isMessage()) {
      var message = eventSupport.getMessage();

      element.type = BpmnElementType.MESSAGE_BOUNDARY;
      element.eventName = message != null ? message.getName() : null;
    } else if (eventSupport.isSignal()) {
      var signal = eventSupport.getSignal();

      element.type = BpmnElementType.SIGNAL_BOUNDARY;
      element.eventName = signal != null ? signal.getName() : null;
    } else if (eventSupport.isTimer()) {
      element.type = BpmnElementType.TIMER_BOUNDARY;
    } else {
      element.type = BpmnElementType.OTHER;
    }
  }

  private void handleIntermediateCatchEvent(BpmnElementImpl element) {
    var event = element.getFlowNode(IntermediateCatchEvent.class);
    var eventSupport = new BpmnEventSupport(event);

    if (eventSupport.isMessage()) {
      var message = eventSupport.getMessage();

      element.type = BpmnElementType.MESSAGE_CATCH;
      element.eventName = message != null ? message.getName() : null;
    } else if (eventSupport.isSignal()) {
      var signal = eventSupport.getSignal();

      element.type = BpmnElementType.SIGNAL_CATCH;
      element.eventName = signal != null ? signal.getName() : null;
    } else if (eventSupport.isTimer()) {
      element.type = BpmnElementType.TIMER_CATCH;
    } else {
      element.type = BpmnElementType.OTHER;
    }
  }

  private void handleIntermediateThrowEvent(BpmnElementImpl element) {
    var event = activity.getFlowNode(IntermediateThrowEvent.class);
    var eventSupport = new BpmnEventSupport(event);

    if (eventSupport.isLink()) {
      element.type = BpmnElementType.LINK_THROW;
    } else {
      element.type = BpmnElementType.OTHER;
    }
  }

  /**
   * Finds and maps the test cases of each process.
   *
   * @return The test cases.
   */
  private TestCasesImpl map() {
    modelInstance.getDefinitions().getChildElementsByType(Process.class).forEach(this::map);
    return this;
  }

  private void map(Process process) {
    processIds.add(process.getId());

    bpmnSupport = new BpmnSupport(process);
    getTestCaseElements(process).stream().map(this::mapTestCase).forEach(testCases::add);
    bpmnSupport = null;
  }

  /**
   * Maps the given test case element on a test case, using the current BPMN support.
   *
   * @param element A test case, defined within {@code bpmndt} extension element.
   * @return The mapped test case.
   */
  private TestCase mapTestCase(TestCaseElement element) {
    var testCase = new TestCaseImpl();
    testCase.element = element;
    testCase.process = bpmnSupport.getProcess();

    if (element.getPath() == null) {
      return testCase;
    }

    var flowNodeIds = element.getPath().getFlowNodeIds();
    for (int i = 0; i < flowNodeIds.size(); i++) {
      var flowNodeId = flowNodeIds.get(i);

      if (!bpmnSupport.has(flowNodeId)) {
        testCase.addInvalidElementId(flowNodeId);
        continue;
      }

      var element = new BpmnElementImpl();
      element.flowNode = bpmnSupport.get(flowNodeId);
      element.multiInstanceLoopCharacteristics = bpmnSupport.getMultiInstanceLoopCharacteristics(flowNodeId);

      if (bpmnSupport.isCallActivity(flowNodeId)) {
        element.type = BpmnElementType.CALL_ACTIVITY;
      } else if (bpmnSupport.isEventBasedGateway(flowNodeId)) {
        element.type = BpmnElementType.EVENT_BASED_GATEWAY;
      } else if (bpmnSupport.isUserTask(flowNodeId)) {
        element.type = BpmnElementType.USER_TASK;
      } else if (bpmnSupport.isIntermediateCatchEvent(flowNodeId)) {
        handleIntermediateCatchEvent(element);
      } else if (bpmnSupport.isBoundaryEvent(flowNodeId)) {
        handleBoundaryEvent(element);
      } else if (bpmnSupport.isReceiveTask(flowNodeId)) {
        // handle receive task as message catch event
        var message = element.getFlowNode(ReceiveTask.class).getMessage();

        element.type = BpmnElementType.MESSAGE_CATCH;
        element.eventName = message != null ? message.getName() : null;
      } else if (bpmnSupport.isIntermediateThrowEvent(flowNodeId)) {
        handleIntermediateThrowEvent(element);
      } else {
        element.type = BpmnElementType.OTHER;
      }

      if (i == 0) {
        element.processStart = bpmnSupport.isProcessStart(flowNodeId);
      }
      if (i == flowNodeIds.size() - 1) {
        element.processEnd = bpmnSupport.isProcessEnd(flowNodeId);
      }

      addElement(testCase, element);
    }

    return testCase;
  }
}
