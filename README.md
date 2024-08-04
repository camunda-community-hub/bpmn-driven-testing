# BPMN Driven Testing
[![](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
[![](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)
![Compatible with: Camunda Platform 7](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%207-26d07c)
![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-26d07c)
[![Maven plugin](https://img.shields.io/maven-central/v/org.camunda.community/bpmn-driven-testing-maven-plugin.svg?label=Maven%20plugin)](https://central.sonatype.com/artifact/org.camunda.community/bpmn-driven-testing-maven-plugin/versions)
[![Gradle plugin](https://img.shields.io/maven-central/v/org.camunda.community/bpmn-driven-testing-gradle-plugin.svg?label=Gradle%20plugin)](https://central.sonatype.com/artifact/org.camunda.community/bpmn-driven-testing-gradle-plugin/versions)
[![Maven plugin 8](https://img.shields.io/maven-central/v/org.camunda.community/bpmn-driven-testing-8-maven-plugin.svg?label=Maven%20plugin%208)](https://central.sonatype.com/artifact/org.camunda.community/bpmn-driven-testing-8-maven-plugin/versions)
[![Gradle plugin 8](https://img.shields.io/maven-central/v/org.camunda.community/bpmn-driven-testing-8-gradle-plugin.svg?label=Gradle%20plugin%208)](https://central.sonatype.com/artifact/org.camunda.community/bpmn-driven-testing-8-gradle-plugin/versions)

[Camunda Platform 7](https://docs.camunda.org/manual/latest/) / [Camunda Platform 8](https://docs.camunda.io/) extension, which is able to generate test code based on an extended BPMN model.

The extension lets the developer focus on testing the business logic, rather than writing boilerplate code.
The generated test code handles process instance start at any selected flow node and advances a process instance in case of wait states.
Since the test code is generated, there is no need to deal with process definition keys and flow node IDs.
Moreover any breaking changes (e.g. a user task becomes a service task) in the BPMN process will already be visible at design time as test compile errors.

**A developer can solely focus on testing the actual implementation!**

The extension consists of:

- Camunda Modeler plugin for a visual selection and the management of test cases
- Maven and Gradle plugins for the generation of JUnit 5 based test code

## Installation, configuration and usage
For information on how to install, configure and use the plugins visit:
- [Camunda Modeler plugin](camunda-modeler-plugin)
- [bpmn-driven-testing-maven-plugin](maven-plugin)
- [bpmn-driven-testing-8-maven-plugin](maven-plugin-8) (Camunda Platform 8)
- [bpmn-driven-testing-gradle-plugin](gradle-plugin)
- [bpmn-driven-testing-8-gradle-plugin](gradle-plugin-8) (Camunda Platform 8)

:warning: Version [0.10.0](https://github.com/camunda-community-hub/bpmn-driven-testing/tree/0.10.0) supports [Camunda 7.21](https://docs.camunda.org/manual/7.21/) (Java 11+) and drops test code generation for JUnit 4. For older Camunda versions (Java 8+) and JUnit 4 support, please rely on version [0.9.0](https://github.com/camunda-community-hub/bpmn-driven-testing/tree/0.9.0).

## Features
- Visual test case selection
- Automatic path finding with
  - Support for embedded sub processes
  - Support for boundary events
  - BPMN collaborations with multiple expanded participants
  - Loop detection
- Test case validation and migration, when a BPMN process was changed - see [docs](docs/test-case-validation-and-migration.md)
- Testing of arbitrary paths through a BPMN process
- Test case generation with
  - Automatic process deployment and process instance start
  - Automatic handling of wait states
  - Call activity stubbing/simulation for isolated testing
  - Fluent API to override default behavior, using BPMN element specific handler

### Details
| Feature | Camunda Platform 7 | Camunda Platform 8 |
|:--------|:-------------------|:-------------------|
| Call activity support | Supported via stubbing - see [test](integration-tests/advanced/src/test/java/org/example/it/CallActivityWithMappingTest.java) | Supported via simulation. `TestCaseExecutor#simulateProcess` must be called for every BPMN process ID that should be simulated - see [test](integration-tests-8/simple/src/test/java/org/example/it/SimpleCallActivityTest.java) |
| Multi instance support | Multi instance activities and embedded subprocesses are supported - see [tests](integration-tests/advanced-multi-instance/src/test/java/org/example/it) | No test code generation implemented yet. But a possibility to write custom test code to handle and verify multi instances exists - see [test](integration-tests-8/advanced-multi-instance/src/test/java/org/example/it/ScopeSequentialTest.java) |
| Spring/Spring Boot test support | Supported - see `advanced-spring*` projects under [integration tests](integration-tests/) | Not needed, since the `TestCaseExecutor` requires only a `ZeebeTestEngine` instance that can be injected via `@ZeebeProcessTest` or `@ZeebeSpringTest` or be manually created |
| Process test coverage extension support | Supported - see `coverage*` projects under [integration tests](integration-tests/) | Not verified yet |

### Handler
Handler classes provide APIs to perform BPMN element specific verifications and actions (e.g. complete a user task with variables or execute custom application code that completes a user task). Handler instances must be accessed via `handle*` methods of generated test cases.

For Camunda Platform 7:
- [CallActivityHandler](impl/src/main/java/org/camunda/community/bpmndt/api/CallActivityHandler.java)
- [EventHandler](impl/src/main/java/org/camunda/community/bpmndt/api/EventHandler.java) for conditional, message and signal intermediate catch or boundary events
- [ExternalTaskHandler](impl/src/main/java/org/camunda/community/bpmndt/api/ExternalTaskHandler.java)
- [JobHandler](impl/src/main/java/org/camunda/community/bpmndt/api/JobHandler.java) for asynchronous continuation and timer catch events
- [MultiInstanceHandler](impl/src/main/java/org/camunda/community/bpmndt/api/MultiInstanceHandler.java) for multi instance activities
- [MultiInstanceScopeHandler](impl/src/main/java/org/camunda/community/bpmndt/api/MultiInstanceScopeHandler.java) for multi instance scopes (e.g. embedded subprocess)
- [ReceiveTaskHandler](impl/src/main/java/org/camunda/community/bpmndt/api/ReceiveTaskHandler.java)
- [UserTaskHandler](impl/src/main/java/org/camunda/community/bpmndt/api/UserTaskHandler.java)

For Camunda Platform 8:
- [CallActivityHandler](impl-8/src/main/java/org/camunda/community/bpmndt/api/CallActivityHandler.java)
- [CustomMultiInstanceHandler](impl-8/src/main/java/org/camunda/community/bpmndt/api/CustomMultiInstanceHandler.java) for a custom handling of multi instance activities and scopes
- [JobHandler](impl-8/src/main/java/org/camunda/community/bpmndt/api/JobHandler.java) for service, script, send or business rule tasks as well as intermediate message throw or message end events
- [MessageEventHandler](impl-8/src/main/java/org/camunda/community/bpmndt/api/MessageEventHandler.java) for intermediate catch and boundary message events
- [OutboundConnectorHandler](impl-8/src/main/java/org/camunda/community/bpmndt/api/OutboundConnectorHandler.java)
- [ReceiveTaskHandler](impl-8/src/main/java/org/camunda/community/bpmndt/api/ReceiveTaskHandler.java)
- [SignalEventHandler](impl-8/src/main/java/org/camunda/community/bpmndt/api/SignalEventHandler.java) for intermediate catch and boundary signal events
- [TimerEventHandler](impl-8/src/main/java/org/camunda/community/bpmndt/api/TimerEventHandler.java) for intermediate catch and boundary timer events
- [UserTaskHandler](impl-8/src/main/java/org/camunda/community/bpmndt/api/UserTaskHandler.java)

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
          <bpmndt:node>itemsAvailableFork</bpmndt:node>
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

### Generate test code
To generate the code for the selected test cases, a developer must run the **generator** goal of the Maven or the **generateTestCases** task of the Gradle plugin.

The plugin finds all *.bpmn files under `src/main/resources` and looks for BPMN processes with a `bpmndt:testCases` extension element.
Each test case will result in a [JUnit 5 extension](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/extension/Extension.html) - in this example: `generated.order_fulfillment.TC_Happy_Path`.

### Camunda 7: Implement tests
The generated test case class - in this example, `TC_Happy_Path` - must be imported and used as a JUnit 5 extension (a `public` or package-private field, which is annotated with `@RegisterExtension`).

```java
@RegisterExtension
public TC_Happy_Path tc = new TC_Happy_Path();
```

Calling `createExecutor()` on the test extension, provides a fluent API,
which is used to specify variables, business key and/or [beans](https://docs.camunda.org/manual/latest/user-guide/testing/#resolving-beans-without-spring-cdi) that are considered during test case execution.
After the specification, `execute()` is called to create a new process instance and exeute the test case.

Moreover the default behavior of wait states and call activities can be adjusted using fluent APIs.
For each applicable flow node a `handle*` method is generated - for example: `handleCheckAvailabilityUserTask()`.

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.order_fulfillment.TC_Happy_Path;

class OrderFulfillmentTest {

  @RegisterExtension
  TC_Happy_Path tc = new TC_Happy_Path();

  @Test
  void testItemsAvailable() {
    // fluent API for user task "checkAvailabilityUserTask"
    tc.handleCheckAvailabilityUserTask()
        .verify((pi, task) -> {
          // verify wait state
          pi.variables().containsEntry("customerId", 123);

          task.hasCandidateGroup("group-xyz");
        })
        .withVariable("available", true)
        .complete();

    // enrich and execute test case
    tc.createExecutor()
        .withBusinessKey("order-20210623-0001")
        .withVariable("customerId", 123)
        .withVariable("customerType", "NEW")
        .withBean("approveOrder", new ApproveOrderDelegate())
        .verify(pi -> {
          // verify state after execution
          pi.isEnded();
        })
        .execute();
  }
}
```

When a test is executed, the generated code handles the creation of the process engine as well as the process definition deployment.
On the other hand, the test case execution handles the process instance start, applies the specified behavior and verifies that the process instance has passed the correct activities.

### Camunda 8: Implement tests
The generated test case class - in this example, `TC_Happy_Path` - must be imported and used as a JUnit 5 extension (a `public` or package-private field, which is annotated with `@RegisterExtension`).

```java
@RegisterExtension
public TC_Happy_Path tc = new TC_Happy_Path();
```

When calling `createExecutor()` on the test extension, a `ZeebeTestEngine` must be provided. The fluent `TestCaseExecutor` API allows to specificy variables, add additional resources, prepare the simulation of called processes and finally start a test case execution via `execute()`.

Moreover the default behavior of wait states and call activities can be adjusted using fluent APIs.
For each applicable flow node a `handle*` method is generated - for example: `handleCheckAvailabilityUserTask()` or `handleApproveOrderSendTask()`.

```java
import static com.google.common.truth.Truth.assertThat;

import org.example.ApproveOrderHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import generated.order_fulfillment.TC_Happy_Path;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;

@ZeebeProcessTest
class OrderFulfillmentTest {

  @RegisterExtension
  TC_Happy_Path tc = new TC_Happy_Path();

  ZeebeTestEngine engine;

  @Test
  void testItemsAvailable() {
    // fluent API for user task "checkAvailabilityUserTask"
    tc.handleCheckAvailabilityUserTask()
        .verify(piAssert -> {
          // verify wait state
          piAssert.hasVariableWithValue("customerId", 123);
        })
        .verifyCandidateGroups(groups -> assertThat(groups).containsExactly("group-xyz"))
        .verifyDueDate(dueDate -> assertThat(dueDate).isEqualTo("2023-02-17T00:00Z"))
        .verifyFormKey("checkAvailability")
        .withVariable("available", true)
        .complete();

    // fluent API for service task "approveOrderSendTask"
    tc.handleApproveOrderSendTask()
        .verifyRetries(3)
        .verifyType("approveOrder");

    var workerBuilder = client.newWorker().jobType("approveOrder").handler(new ApproveOrderHandler());

    // run worker, while test case is being executed
    try (var worker = workerBuilder.open()) {
      // enrich and execute test case
      tc.createExecutor(engine)
        .withVariable("orderId", "order-20210623-0001")
        .withVariable("customerId", 123)
        .withVariable("customerType", "NEW")
        .verify(piAssert -> {
          // verify state after execution
          piAssert.isCompleted();
        })
        .execute();
    }
  }
}
```

When a test case is executed, a `ZeebeClient` is used to deploy test case related resources (*.bpmn, *.dmn and/or *.form), create a process instance and interact with the `ZeebeTestEngine` by applying the specified behavior and verifying that the process instance has passed the correct BPMN elements.

## More screenshots

| ![order-fulfillment-canceled-by-customer.png](docs/order-fulfillment-canceled-by-customer.png) | 
|:--:| 
| *Arbitrary paths* |

| ![order-fulfillment-invalid-path.png](docs/order-fulfillment-invalid-path.png) | 
|:--:| 
| *Test case validation* |
