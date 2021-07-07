package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.FIND_EVENT_SUBSCRIPTION;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_INSTANCE;

import java.util.Optional;
import java.util.function.Function;

import org.camunda.bpm.extension.bpmndt.BpmnNode;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.Message;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;

import com.squareup.javapoet.CodeBlock;

public class HandleIntermediateCatchEventCodeBlock implements Function<BpmnNode, CodeBlock> {

  @Override
  public CodeBlock apply(BpmnNode node) {
    IntermediateCatchEvent event = node.as(IntermediateCatchEvent.class);

    CodeBlock.Builder builder = CodeBlock.builder();

    builder.addStatement("assertThat($L).isWaitingAt($S)", PROCESS_INSTANCE, node.getId());

    Optional<EventDefinition> eventDefinition = event.getEventDefinitions().stream().findFirst();
    if (!eventDefinition.isPresent()) {
      builder.add("// no event definition found for intermediate message catch event $L\n", node.getId());
    } else if (eventDefinition.get() instanceof MessageEventDefinition) {
      Message message = ((MessageEventDefinition) eventDefinition.get()).getMessage();

      handleEventSubscription(builder, node, message != null ? message.getName() : null);
    } else if (eventDefinition.get() instanceof SignalEventDefinition) {
      Signal signal = ((SignalEventDefinition) eventDefinition.get()).getSignal();

      handleEventSubscription(builder, node, signal != null ? signal.getName() : null);
    } else if (eventDefinition.get() instanceof TimerEventDefinition) {
      builder.addStatement("$L(job($S, $L))", node.getLiteral(), node.getId(), PROCESS_INSTANCE);
    } else {
      builder.add("// unsupported intermediate catch event $L\n", node.getId());
    }

    return builder.build();
  }

  protected void handleEventSubscription(CodeBlock.Builder builder, BpmnNode node, String eventName) {
    Object[] args = {node.getLiteral(), FIND_EVENT_SUBSCRIPTION, node.getId(), eventName};
    // e.g.: messageEvent(findEventSubscription("messageEvent", "simpleMessage"))
    builder.addStatement("$L($L($S, $S))", args);
  }
}
