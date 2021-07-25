# BPMN Driven Testing
[![](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)

Camunda extension, which is able to generate JUnit tests based on an extended BPMN model.

The extension lets the developer focus on testing the business logic, rather than writing boilerplate code.
The generated test code handles process instance start at any selected flow node and advances a process instance in case of waiting states.
Since the test code is generated, there is no need to deal with process definition keys and flow node IDs.
If the BPMN model changes, any breaking changes (e.g. a service task becomes an external task) in the BPMN process will result in test compile errors.

The extension consists of:

- [Camunda Modeler plugin](camunda-modeler-plugin) for a visual selection and the management of test cases
- [Maven plugin](maven-plugin) for generation of JUnit test code

## How does it work?

### Select test cases
After modeling, a developer uses the Camunda Modeler plugin to define suitable test cases by selecting a start and an end flow node.
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
Each test case will result in a [JUnit test rule](https://github.com/junit-team/junit4/wiki/Rules) - in this example: `generated.TC_order_fulfillment__Happy_Path`.

### Implement tests
In this example, `TC_order_fulfillment__Happy_Path` must be imported and used as a JUnit test rule (a `public` field, which is annotated with `@Rule`):

```java
@Rule
public TC_order_fulfillment__Happy_Path tc = new TC_order_fulfillment__Happy_Path();
```

Calling `createExecutor()` on the test rule, provides a fluent API,
which is used to specify variables, business key and/or [mocks](https://docs.camunda.org/manual/7.15/user-guide/testing/#resolving-beans-without-spring-cdi) that are considered during test case execution.
After the specification, `execute()` is called to create a new process instance and exeute the test case.

Moreover the default behavior of waiting states and call activities can be adjusted using fluent APIs.
For each applicable flow node a "handle" method is generated - for example: `handleCheckAvailabilityUserTask()`.

```java
import org.junit.Rule;
import org.junit.Test;

import generated.TC_order_fulfillment__Happy_Path;

public class OrderFulfillmentTest {

  @Rule
  public TC_order_fulfillment__Happy_Path tc = new TC_order_fulfillment__Happy_Path();

  @Test
  public void testItemsAvailable() {
    // fluent API for user task "checkAvailabilityUserTask"
    tc.handleCheckAvailabilityUserTask()
      .verify((pi, task) -> {
        // verify waiting state
        pi.variables().containsEntry("customerId", 123);

        task.hasCandidateGroup("group-xyz");
      })
      .withVariable("available", true)
      .complete();

    // enrich and execute test case
    tc.createExecutor()
      .withBusinessKey("order-123-456")
      .withVariable("customerId", 123)
      .withVariable("items", "[{\"id\": 1, \"quantity\": 3},{\"id\": 7, \"quantity\": 1}]")
      .withMock("approveOrder", new ApproveOrderDelegate())
      .verify(pi -> {
        // verify state after execution
        pi.isEnded();
      })
      .execute();
  }
}
```

When a test is started, the generated test rule handles the creation of the process engine as well as the process definition deployment.
On the other hand, the test case execution handles the process instance start, applies the specified behavior and verifies that the process instance has passed the correct activities.

A developer can solely focus on the actual implementation!

## Features
- Visual test case selection with
  - Support for embedded sub processes
  - Support for boundary events
  - BPMN collaborations with one expanded participant
  - Loop detection
- Generated test cases provide
  - Automatic handling of waiting states
  - Call activity stubbing for isolated testing - see [CallActivityTest](maven-plugin/src/test/it/advanced/src/test/java/org/example/it/CallActivityTest.java)
  - Fluent API to override default behavior
- Spring test support - see [integration test](maven-plugin/src/test/it/advanced-spring)
- Testing of arbitrary paths through a BPMN process

![order-fulfillment-canceled-by-customer.png](docs/order-fulfillment-canceled-by-customer.png)
