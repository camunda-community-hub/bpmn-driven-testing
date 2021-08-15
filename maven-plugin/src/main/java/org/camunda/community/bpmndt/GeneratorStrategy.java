package org.camunda.community.bpmndt;

import java.lang.reflect.Type;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * Strategy, used when generating a test case.
 */
public interface GeneratorStrategy {

  void addHandlerField(TypeSpec.Builder classBuilder);

  void addHandlerFieldAfter(TypeSpec.Builder classBuilder);

  void addHandlerFieldBefore(TypeSpec.Builder classBuilder);

  void addHandlerMethod(TypeSpec.Builder classBuilder);

  void addHandlerMethodAfter(TypeSpec.Builder classBuilder);

  void addHandlerMethodBefore(TypeSpec.Builder classBuilder);

  void applyHandler(MethodSpec.Builder methodBuilder);

  void applyHandlerAfter(MethodSpec.Builder methodBuilder);

  void applyHandlerBefore(MethodSpec.Builder methodBuilder);

  Type getHandlerType();

  void initHandler(MethodSpec.Builder methodBuilder);

  void initHandlerAfter(MethodSpec.Builder methodBuilder);

  void initHandlerBefore(MethodSpec.Builder methodBuilder);
}
