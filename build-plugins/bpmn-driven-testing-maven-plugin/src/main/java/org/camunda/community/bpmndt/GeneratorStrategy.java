package org.camunda.community.bpmndt;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
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

  TypeName getHandlerType();

  void initHandler(MethodSpec.Builder methodBuilder);

  void initHandlerAfter(MethodSpec.Builder methodBuilder);

  void initHandlerBefore(MethodSpec.Builder methodBuilder);

  CodeBlock initHandlerStatement();

  /**
   * Determines if an asynchronous continuation after the activity should be handled or not.
   * 
   * @return {@code true}, if it should be handled. Otherwise {@code false}.
   */
  boolean shouldHandleAfter();

  /**
   * Determines if an asynchronous continuation before the activity should be handled or not.
   * 
   * @return {@code true}, if it should be handled. Otherwise {@code false}.
   */
  boolean shouldHandleBefore();
}
