package org.camunda.bpm.extension.bpmndt.impl.generation;

import java.util.function.Supplier;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;
import org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants;

import com.squareup.javapoet.MethodSpec;

public class AssertThatPi implements Supplier<MethodSpec> {

  @Override
  public MethodSpec get() {
    return MethodSpec.methodBuilder(GeneratorConstants.ASSERT_THAT_PI)
        .addModifiers(Modifier.PROTECTED)
        .returns(ProcessInstanceAssert.class)
        .addStatement("return $T.assertThat($L)", ProcessEngineTests.class, GeneratorConstants.PROCESS_INSTANCE)
        .build();
  }
}
