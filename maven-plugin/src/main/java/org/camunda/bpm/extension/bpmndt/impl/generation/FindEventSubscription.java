package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.ACTIVITY_ID;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.EVENT_NAME;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.FIND_EVENT_SUBSCRIPTION;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_RULE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_INSTANCE;

import java.util.function.Supplier;

import org.camunda.bpm.engine.runtime.EventSubscription;

import com.squareup.javapoet.MethodSpec;

/**
 * Helper method, used to find the {@link EventSubscription} for a specific activity and event name.
 * 
 * @see HandleIntermediateCatchEventCodeBlock
 */
public class FindEventSubscription implements Supplier<MethodSpec> {

  @Override
  public MethodSpec get() {
    return MethodSpec.methodBuilder(FIND_EVENT_SUBSCRIPTION)
        .returns(EventSubscription.class)
        .addParameter(String.class, ACTIVITY_ID)
        .addParameter(String.class, EVENT_NAME)
        .addCode("return $L.getRuntimeService().createEventSubscriptionQuery()\n", PROCESS_ENGINE_RULE)
        .addCode("    .processInstanceId($L.getId())\n", PROCESS_INSTANCE)
        .addCode("    .activityId($L)\n", ACTIVITY_ID)
        .addCode("    .eventName($L)\n", EVENT_NAME)
        .addCode("    .singleResult();\n")
        .build();
  }
}
