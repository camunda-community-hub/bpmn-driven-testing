package org.camunda.community.bpmndt.model;

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

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BoundaryEvent;
import org.camunda.bpm.model.bpmn.instance.Error;
import org.camunda.bpm.model.bpmn.instance.Escalation;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ReceiveTask;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.bpm.model.xml.ModelParseException;
import org.camunda.community.bpmndt.model.element.TestCaseElement;
import org.camunda.community.bpmndt.model.element.TestCasesElement;

class TestCasesImpl implements TestCases {

  static {
    // set extended BPMN instance to be able to use the custom extension elements
    Bpmn.INSTANCE = BpmnExtension.INSTANCE;
  }

  static TestCasesImpl of(InputStream stream) {
    try {
      return of(Bpmn.readModelFromStream(stream));
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

  protected final BpmnModelInstance modelInstance;

  private final Set<String> processIds;
  private final List<TestCase> testCases;

  private BpmnSupport bpmnSupport;

  TestCasesImpl(BpmnModelInstance modelInstance) {
    this.modelInstance = modelInstance;

    processIds = new TreeSet<>();
    testCases = new LinkedList<>();
  }

  private void addActivity(TestCaseImpl testCase, TestCaseActivityImpl next) {
    testCase.addActivity(next);

    TestCaseActivityScopeImpl scope = addActivityScope(testCase, next.getId());
    if (scope != null) {
      scope.addActivity(next);
    }
  }

  /**
   * Adds the activity's parent as scope and returns it.
   * 
   * @param testCase The current test case.
   * 
   * @param elementId The current element (activity) ID.
   * 
   * @return The parent scope or {@code null}, if the parent is the process.
   */
  private TestCaseActivityScopeImpl addActivityScope(TestCaseImpl testCase, String elementId) {
    String parentElementId = bpmnSupport.getParentElementId(elementId);
    if (testCase.getProcessId().equals(parentElementId)) {
      return null;
    }

    TestCaseActivityScopeImpl scope = testCase.getActivityScope(parentElementId);
    if (scope == null) {
      scope = new TestCaseActivityScopeImpl();
      scope.flowNode = bpmnSupport.get(parentElementId);
      scope.multiInstanceLoopCharacteristics = bpmnSupport.getMultiInstanceLoopCharacteristics(parentElementId);

      testCase.addActivityScope(scope);

      scope.parent = addActivityScope(testCase, scope.getId());
    }

    return scope;
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

  protected List<TestCaseElement> getTestCaseElements(Process process) {
    if (process.getExtensionElements() == null) {
      return Collections.emptyList();
    }

    TestCasesElement testCasesElement = (TestCasesElement) process.getExtensionElements().getUniqueChildElementByType(TestCasesElement.class);
    if (testCasesElement == null) {
      return Collections.emptyList();
    }

    return testCasesElement.getTestCases();
  }

  private void handleBoundaryEvent(TestCaseActivityImpl activity) {
    BoundaryEvent event = activity.getFlowNode(BoundaryEvent.class);

    activity.attachedTo = event.getAttachedTo().getId();

    BpmnEventSupport eventSupport = new BpmnEventSupport(event);

    if (eventSupport.isConditional()) {
      activity.type = TestCaseActivityType.CONDITIONAL_BOUNDARY;
    } else if (eventSupport.isError()) {
      Error error = eventSupport.getError();

      activity.type = TestCaseActivityType.ERROR_BOUNDARY;
      activity.eventCode = error != null ? error.getErrorCode() : null;
    } else if (eventSupport.isEscalation()) {
      Escalation escalation = eventSupport.getEscalation();

      activity.type = TestCaseActivityType.ESCALATION_BOUNDARY;
      activity.eventCode = escalation != null ? escalation.getEscalationCode() : null;
    } else if (eventSupport.isMessage()) {
      Message message = eventSupport.getMessage();

      activity.type = TestCaseActivityType.MESSAGE_BOUNDARY;
      activity.eventName = message != null ? message.getName() : null;
    } else if (eventSupport.isSignal()) {
      Signal signal = eventSupport.getSignal();

      activity.type = TestCaseActivityType.SIGNAL_BOUNDARY;
      activity.eventName = signal != null ? signal.getName() : null;
    } else if (eventSupport.isTimer()) {
      activity.type = TestCaseActivityType.TIMER_BOUNDARY;
    } else {
      activity.type = TestCaseActivityType.OTHER;
    }
  }

  private void handleIntermediateCatchEvent(TestCaseActivityImpl activity) {
    IntermediateCatchEvent event = activity.getFlowNode(IntermediateCatchEvent.class);

    BpmnEventSupport eventSupport = new BpmnEventSupport(event);

    if (eventSupport.isConditional()) {
      activity.type = TestCaseActivityType.CONDITIONAL_CATCH;
    } else if (eventSupport.isMessage()) {
      Message message = eventSupport.getMessage();

      activity.type = TestCaseActivityType.MESSAGE_CATCH;
      activity.eventName = message != null ? message.getName() : null;
    } else if (eventSupport.isSignal()) {
      Signal signal = eventSupport.getSignal();

      activity.type = TestCaseActivityType.SIGNAL_CATCH;
      activity.eventName = signal != null ? signal.getName() : null;
    } else if (eventSupport.isTimer()) {
      activity.type = TestCaseActivityType.TIMER_CATCH;
    } else {
      activity.type = TestCaseActivityType.OTHER;
    }
  }

  private void handleIntermediateThrowEvent(TestCaseActivityImpl activity) {
    IntermediateThrowEvent event = activity.getFlowNode(IntermediateThrowEvent.class);

    BpmnEventSupport eventSupport = new BpmnEventSupport(event);

    if (eventSupport.isLink()) {
      activity.type = TestCaseActivityType.LINK_THROW;
    } else {
      activity.type = TestCaseActivityType.OTHER;
    }
  }

  @Override
  public boolean isEmpty() {
    return testCases.isEmpty();
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
   * 
   * @return The mapped test case.
   */
  private TestCase mapTestCase(TestCaseElement element) {
    TestCaseImpl testCase = new TestCaseImpl();
    testCase.element = element;
    testCase.process = bpmnSupport.getProcess();

    if (element.getPath() == null) {
      return testCase;
    }

    List<String> flowNodeIds = element.getPath().getFlowNodeIds();
    for (int i = 0; i < flowNodeIds.size(); i++) {
      String flowNodeId = flowNodeIds.get(i);

      if (!bpmnSupport.has(flowNodeId)) {
        testCase.addInvalidFlowNodeId(flowNodeId);
        continue;
      }

      TestCaseActivityImpl activity = new TestCaseActivityImpl();
      activity.flowNode = bpmnSupport.get(flowNodeId);
      activity.multiInstanceLoopCharacteristics = bpmnSupport.getMultiInstanceLoopCharacteristics(flowNodeId);

      if (bpmnSupport.isCallActivity(flowNodeId)) {
        activity.type = TestCaseActivityType.CALL_ACTIVITY;
      } else if (bpmnSupport.isEventBasedGateway(flowNodeId)) {
        activity.type = TestCaseActivityType.EVENT_BASED_GATEWAY;
      } else if (bpmnSupport.isExternalTask(flowNodeId)) {
        activity.type = TestCaseActivityType.EXTERNAL_TASK;
        activity.topicName = bpmnSupport.getTopicName(flowNodeId);
      } else if (bpmnSupport.isUserTask(flowNodeId)) {
        activity.type = TestCaseActivityType.USER_TASK;
      } else if (bpmnSupport.isIntermediateCatchEvent(flowNodeId)) {
        handleIntermediateCatchEvent(activity);
      } else if (bpmnSupport.isBoundaryEvent(flowNodeId)) {
        handleBoundaryEvent(activity);
      } else if (bpmnSupport.isReceiveTask(flowNodeId)) {
        Message message = activity.getFlowNode(ReceiveTask.class).getMessage();

        activity.type = TestCaseActivityType.RECEIVE_TASK;
        activity.eventName = message != null ? message.getName() : null;
      } else if (bpmnSupport.isIntermediateThrowEvent(flowNodeId)) {
        handleIntermediateThrowEvent(activity);
      } else {
        activity.type = TestCaseActivityType.OTHER;
      }

      if (i == 0) {
        activity.processStart = bpmnSupport.isProcessStart(flowNodeId);
      }
      if (i == flowNodeIds.size() - 1) {
        activity.processEnd = bpmnSupport.isProcessEnd(flowNodeId);
      }

      addActivity(testCase, activity);
    }

    return testCase;
  }
}
