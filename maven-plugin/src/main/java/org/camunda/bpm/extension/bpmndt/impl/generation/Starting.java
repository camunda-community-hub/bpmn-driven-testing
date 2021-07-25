package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.STARTING;

import java.util.Optional;
import java.util.function.BiFunction;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.extension.bpmndt.BpmnNode;
import org.camunda.bpm.extension.bpmndt.BpmnSupport;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.camunda.bpm.extension.bpmndt.type.TestCase;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;
import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.ExternalTaskHandler;
import org.camunda.community.bpmndt.api.IntermediateCatchEventHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.junit.runner.Description;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

public class Starting implements BiFunction<GeneratorContext, TestCase, MethodSpec> {

  @Override
  public MethodSpec apply(GeneratorContext context, TestCase testCase) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder(STARTING)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .addParameter(Description.class, "description");

    // call #starting of superclass
    builder.addStatement("super.$L(description)", STARTING);

    BpmnSupport bpmnSupport = context.getBpmnSupport();

    // build flow node specific code blocks
    for (String flowNodeId : testCase.getPath().getFlowNodeIds()) {
      if (!bpmnSupport.has(flowNodeId)) {
        continue;
      }

      BpmnNode node = bpmnSupport.get(flowNodeId);

      if (node.isAsyncBefore()) {
        builder.addCode("\n// $L: $L\n", node.getType(), node.getId());
        Object[] args = {String.format("%sBefore", node.getLiteral()), JobHandler.class, node.getId()};
        builder.addStatement("$L = new $T(getProcessEngine(), $S)",  args);
      }
      if (node.isCallActivity()) {
        builder.addCode("\n// $L: $L\n", node.getType(), node.getId());
        Object[] args = {node.getLiteral(), CallActivityHandler.class, node.getId()};
        builder.addStatement("$L = new $T(instance, $S)", args);
      }
      if (node.isExternalTask()) {
        ServiceTask serviceTask = node.as(ServiceTask.class);

        builder.addCode("\n// $L: $L\n", node.getType(), node.getId());
        Object[] args = {node.getLiteral(), ExternalTaskHandler.class, serviceTask.getCamundaTopic()};
        builder.addStatement("$L = new $T(getProcessEngine(), $S)", args);
      }
      if (node.isIntermediateCatchEvent()) {
        builder.addCode("\n// $L: $L\n", node.getType(), node.getId());
        builder.addCode(applyIntermediateCatchEvent(node));
      }
      if (node.isUserTask()) {
        builder.addCode("\n// $L: $L\n", node.getType(), node.getId());
        Object[] args = {node.getLiteral(), UserTaskHandler.class, node.getId()};
        builder.addStatement("$L = new $T(getProcessEngine(), $S)", args);
      }
      if (node.isAsyncAfter()) {
        builder.addCode("\n// $L: $L\n", node.getType(), node.getId());
        Object[] args = {String.format("%sAfter", node.getLiteral()), JobHandler.class, node.getId()};
        builder.addStatement("$L = new $T(getProcessEngine(), $S)", args);
      }
    }

    return builder.build();
  }

  protected CodeBlock applyIntermediateCatchEvent(BpmnNode node) {
    IntermediateCatchEvent event = node.as(IntermediateCatchEvent.class);

    CodeBlock.Builder builder = CodeBlock.builder();

    Optional<EventDefinition> eventDefinition = event.getEventDefinitions().stream().findFirst();
    if (!eventDefinition.isPresent()) {
      builder.add("// $L: no event definition found for intermediate message catch event\n", node.getId());
    } else if (eventDefinition.get() instanceof MessageEventDefinition) {
      Message message = ((MessageEventDefinition) eventDefinition.get()).getMessage();

      Object[] args = {node.getLiteral(), IntermediateCatchEventHandler.class, node.getId(), message != null ? message.getName() : null};
      builder.addStatement("$L = new $T(getProcessEngine(), $S, $S)", args);
    } else if (eventDefinition.get() instanceof SignalEventDefinition) {
      Signal signal = ((SignalEventDefinition) eventDefinition.get()).getSignal();

      Object[] args = {node.getLiteral(), IntermediateCatchEventHandler.class, node.getId(), signal != null ? signal.getName() : null};
      builder.addStatement("$L = new $T(getProcessEngine(), $S, $S)", args);
    } else if (eventDefinition.get() instanceof TimerEventDefinition) {
      Object[] args = {node.getLiteral(), JobHandler.class, node.getId()};
      builder.addStatement("$L = new $T(getProcessEngine(), $S)", args);
    } else {
      builder.add("// $L: unsupported intermediate catch event\n", node.getId());
    }

    return builder.build();
  }
}
