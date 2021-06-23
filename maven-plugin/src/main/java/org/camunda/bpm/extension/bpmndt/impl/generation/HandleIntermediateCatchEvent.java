package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.EVENT_SUBSCRIPTION;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.JOB;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_RULE;

import java.util.Optional;
import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.extension.bpmndt.BpmnNode;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;

import com.squareup.javapoet.MethodSpec;

public class HandleIntermediateCatchEvent implements Function<BpmnNode, MethodSpec> {

  @Override
  public MethodSpec apply(BpmnNode node) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder(node.getLiteral())
        .addModifiers(Modifier.PROTECTED);

    IntermediateCatchEvent event = node.as(IntermediateCatchEvent.class);

    Optional<EventDefinition> eventDefinition = event.getEventDefinitions().stream().findFirst();
    if (eventDefinition.isEmpty()) {
      builder.addCode("// no event definition found for intermediate catch event $L\n", node.getId());
    } else if (eventDefinition.get() instanceof MessageEventDefinition) {
      builder.addJavadoc("Overwrite to handle intermediate message catch event $L.", node.getId());
      builder.addParameter(EventSubscription.class, EVENT_SUBSCRIPTION);

      Object[] args = {PROCESS_ENGINE_RULE, EVENT_SUBSCRIPTION, EVENT_SUBSCRIPTION};
      builder.addStatement("$L.getRuntimeService().messageEventReceived($L.getEventName(), $L.getExecutionId())", args);
    } else if (eventDefinition.get() instanceof SignalEventDefinition) {
      builder.addJavadoc("Overwrite to handle intermediate signal catch event $L.", node.getId());
      builder.addParameter(EventSubscription.class, EVENT_SUBSCRIPTION);

      Object[] args = {PROCESS_ENGINE_RULE, EVENT_SUBSCRIPTION, EVENT_SUBSCRIPTION};
      builder.addStatement("$L.getRuntimeService().signalEventReceived($L.getEventName(), $L.getExecutionId())", args);
    } else if (eventDefinition.get() instanceof TimerEventDefinition) {
      builder.addJavadoc("Overwrite to handle intermediate timer catch event $L.", node.getId());
      builder.addParameter(Job.class, JOB);
      builder.addStatement("$L.getManagementService().executeJob($L.getId())", PROCESS_ENGINE_RULE, JOB);
    } else {
      builder.addJavadoc("Overwrite to handle intermediate catch event $L", node.getId());
    }
    
    return builder.build();
  }
}
