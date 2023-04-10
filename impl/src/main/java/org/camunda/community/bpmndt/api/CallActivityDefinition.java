package org.camunda.community.bpmndt.api;

import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;

/**
 * Represents a concrete call activity during test case execution.
 */
public class CallActivityDefinition {

  protected CallableElementBinding binding;
  protected String businessKey;
  protected String definitionKey;
  protected String definitionTenantId;
  protected boolean inputs;
  protected boolean outputs;
  protected Integer version;
  protected String versionTag;

  public CallableElementBinding getBinding() {
    return binding != null ? binding : CallableElementBinding.LATEST;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getDefinitionKey() {
    return definitionKey;
  }

  public String getDefinitionTenantId() {
    return definitionTenantId;
  }

  public Integer getVersion() {
    return version;
  }

  public String getVersionTag() {
    return versionTag;
  }

  /**
   * Determines if the call activity defines a mapping under "In mapping" in tab "Variables".
   * 
   * @return {@code true}, if input variables are mapped. Otherwise {@code false}.
   */
  public boolean hasInputs() {
    return inputs;
  }

  /**
   * Determines if the call activity defines a mapping under "Out mapping" in tab "Variables".
   * 
   * @return {@code true}, if output variables are mapped. Otherwise {@code false}.
   */
  public boolean hasOutputs() {
    return outputs;
  }
}
