package org.camunda.bpm.extension.bpmndt.impl;

import org.camunda.bpm.extension.bpmndt.BpmnSupport;
import org.camunda.bpm.extension.bpmndt.Generator;
import org.camunda.bpm.extension.bpmndt.Generator.Builder;

public class GeneratorBuilderImpl implements Generator.Builder {

  private GeneratorContextImpl context;

  public GeneratorBuilderImpl() {
    context = new GeneratorContextImpl();
  }

  @Override
  public Builder bpmnResourceName(String bpmnResourceName) {
    context.bpmnResourceName = bpmnResourceName;
    return this;
  }

  @Override
  public Builder bpmnSupport(BpmnSupport bpmnSupport) {
    context.bpmnSupport = bpmnSupport;
    return this;
  }

  @Override
  public Generator build() {
    Generator generator = new GeneratorImpl(context);

    // reset
    context = new GeneratorContextImpl();

    return generator;
  }

  @Override
  public Builder packageName(String packageName) {
    context.packageName = packageName;
    return this;
  }

  @Override
  public Builder springEnabled(boolean springEnabled) {
    context.springEnabled = springEnabled;
    return this;
  }
}
