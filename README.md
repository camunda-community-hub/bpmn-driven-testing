# BPMN Driven Testing

[![](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

Camunda extension, which is able to generate JUnit tests based on an extended BPMN model.

The extension lets the developer focus on testing the business logic, rather than writing boilerplate code that starts a process instance at a specific flow node or advances a process instance in case of waiting states.
Since the test code is generated, there is no need to deal with divergent process definition keys and flow node IDs, if the BPMN model changes.
Moreover any breaking changes (e.g. a service task becomes an external task) in the BPMN process will result in test compile errors.

The extension consists of:

- [Camunda Modeler plugin](camunda-modeler-plugin) for a visual selection and the management of test cases
- [Maven plugin](maven-plugin) for generation of JUnit test code

## How does it work?

### Select test cases
After modeling, a developer uses the Camunda Modeler Plugin to define suitable test cases by selecting a start and an end flow node.
The modeler plugin finds all possible paths through the BPMN process. Each path can be added as a test case.

![order-fulfillment-happy-path.png](docs/order-fulfillment-happy-path.png)

Optionally a test case can be named and described. Names and descriptions are reflected in the generated test code.

![order-fulfillment-happy-path-edit.png](docs/order-fulfillment-happy-path-edit.png)

When the BPMN model is saved, the selected test cases are attached to the BPMN process in form of a custom extension element.

```xml
<bpmn:process id="order-fulfillment" isExecutable="true">
  <bpmn:extensionElements>
    <bpmndt:testCases>
      <bpmndt:testCase>
        <bpmndt:name>Happy Path</bpmndt:name>
        <bpmndt:path>
          <bpmndt:node>orderReceivedStartEvent</bpmndt:node>
          <bpmndt:node>checkAvailabilityUserTask</bpmndt:node>
          <bpmndt:node>Gateway_0dw0zxn</bpmndt:node>
          <bpmndt:node>approveOrderSendTask</bpmndt:node>
          <bpmndt:node>prepareOrderUserTask</bpmndt:node>
          <bpmndt:node>deliverOrderUserTask</bpmndt:node>
          <bpmndt:node>orderFulfilledEndEvent</bpmndt:node>
        </bpmndt:path>
      </bpmndt:testCase>
    </bpmndt:testCases>
  </bpmn:extensionElements>

  <!-- ... -->
</bpmn:process>
```

### Generate test cases
To generate the code for the selected test cases, a developer must  run the **generator** goal of the [bpmn-driven-testing-maven-plugin](maven-plugin) - in Eclipse select the project and press **ALT+F5** to update.
The goal finds all *.bpmn files under `src/main/resources` and looks for BPMN processes with a `bpmndt:testCases` extension element.
Each test case will result in an abstract JUnit test case - in this example: `generated.TC_order_fulfillment__Happy_Path`.

### Implement test cases
`TC_order_fulfillment__Happy_Path` must be extended to run as a JUnit test.

By overriding the `before` method, a developer can set process variables and register beans that are needed to test process implementation.
Moreover the default behavior at each waiting state can also be adjusted by overriding the corresponding callback method - in this example: `checkAvailabilityUserTask(Task task)`.

```java
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

import generated.TC_order_fulfillment__Happy_Path;

public class OrderFulfillmentTest extends TC_order_fulfillment__Happy_Path {

  @Override
  protected String before(VariableMap variables) {
    Mocks.register("approveOrder", new ApproveOrderDelegate());

    variables.putValue("customerId", "XYZ");
    variables.putValue("items", "[{\"id\": 1, \"quantity\": 3},{\"id\": 7, \"quantity\": 1}]");

    return "order-123"; // business key
  }

  @Override
  protected void checkAvailabilityUserTask(Task task) {
    rule.getTaskService().complete(task.getId(), Variables.putValue("available", true));
  }

  @Override
  protected void after() {
    assertThatPi().isEnded();
  }
}
```

When a test case is executed, the super class handles the process definition deployment, the process instance start, the execution of the callback methods as well as the assertions, which verify that the process instance has passed the correct activities. A developer can solely focus on the actual implementation!

## Features
- Visual test case selection with
  - Support for embedded sub processes
  - Support for boundary events
  - BPMN collaborations with one expanded participant
  - Loop detection
- Generated test cases provide
  - automatic handling of waiting states
  - sub process stubbing for isolated testing - see [CallActivityTest](maven-plugin/src/test/it/advanced/src/test/java/org/example/it/CallActivityTest.java)
  - callbacks to override default behavior
- Spring test support - see [integration test](maven-plugin/src/test/it/advanced-spring)
- Testing of arbitrary paths through a BPMN process

![order-fulfillment-canceled-by-customer.png](docs/order-fulfillment-canceled-by-customer.png)
