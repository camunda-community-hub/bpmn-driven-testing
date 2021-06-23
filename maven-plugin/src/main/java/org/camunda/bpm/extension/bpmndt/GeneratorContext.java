package org.camunda.bpm.extension.bpmndt;

import com.squareup.javapoet.TypeName;

/**
 * Context, that is used by a {@link Generator} instance.
 */
public interface GeneratorContext {

  String getBpmnResourceName();

  BpmnSupport getBpmnSupport();

  String getPackageName();

  TypeName getTypeName(String simpleName);

  boolean isSpringEnabled();
}
