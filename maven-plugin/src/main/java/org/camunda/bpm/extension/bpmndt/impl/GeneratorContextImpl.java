package org.camunda.bpm.extension.bpmndt.impl;

import org.camunda.bpm.extension.bpmndt.BpmnSupport;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

class GeneratorContextImpl implements GeneratorContext {

  protected String bpmnResourceName;
  protected BpmnSupport bpmnSupport;
  protected String packageName;
  protected boolean springEnabled;

  @Override
  public String getBpmnResourceName() {
    return bpmnResourceName;
  }

  @Override
  public BpmnSupport getBpmnSupport() {
    return bpmnSupport;
  }

  @Override
  public String getPackageName() {
    return packageName;
  }

  @Override
  public TypeName getTypeName(String simpleName) {
    return ClassName.get(packageName, simpleName);
  }

  @Override
  public boolean isSpringEnabled() {
    return springEnabled;
  }
}
