package org.camunda.community.bpmndt.api;

import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;

/**
 * Represents a concrete call activity during test case execution.
 */
public class CallActivityDefinition {

  private CallableElementBinding binding;
  private String businessKey;
  private String definitionKey;
  private String definitionTenantId;
  private Integer version;
  private String versionTag;

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

  public void setBinding(CallableElementBinding binding) {
    this.binding = binding;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public void setDefinitionKey(String definitionKey) {
    this.definitionKey = definitionKey;
  }

  public void setDefinitionTenantId(String definitionTenantId) {
    this.definitionTenantId = definitionTenantId;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public void setVersionTag(String versionTag) {
    this.versionTag = versionTag;
  }
}
