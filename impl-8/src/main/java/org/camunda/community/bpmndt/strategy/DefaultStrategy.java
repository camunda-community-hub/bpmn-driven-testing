package org.camunda.community.bpmndt.strategy;

import org.camunda.community.bpmndt.GeneratorStrategy;
import org.camunda.community.bpmndt.Literal;
import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.CustomMultiInstanceHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.MessageEventHandler;
import org.camunda.community.bpmndt.api.OutboundConnectorHandler;
import org.camunda.community.bpmndt.api.SignalEventHandler;
import org.camunda.community.bpmndt.api.TimerEventHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.camunda.community.bpmndt.model.BpmnElement;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec.Builder;

public class DefaultStrategy implements GeneratorStrategy {

  public static final TypeName CALL_ACTIVITY = TypeName.get(CallActivityHandler.class);
  public static final TypeName CUSTOM_MULTI_INSTANCE = TypeName.get(CustomMultiInstanceHandler.class);
  public static final TypeName JOB = TypeName.get(JobHandler.class);
  public static final TypeName MESSAGE_EVENT = TypeName.get(MessageEventHandler.class);
  public static final TypeName OUTBOUND_CONNECTOR = TypeName.get(OutboundConnectorHandler.class);
  public static final TypeName OTHER = TypeName.get(Void.class);
  public static final TypeName SIGNAL_EVENT = TypeName.get(SignalEventHandler.class);
  public static final TypeName TIMER_EVENT = TypeName.get(TimerEventHandler.class);
  public static final TypeName USER_TASK = TypeName.get(UserTaskHandler.class);

  protected final BpmnElement element;
  protected final String literal;

  public DefaultStrategy(BpmnElement element) {
    this.element = element;

    literal = Literal.toLiteral(element.getId());
  }

  @Override
  public void addHandlerField(Builder classBuilder) {
    // nothing to do here
  }

  @Override
  public void addHandlerMethod(Builder classBuilder) {
    // nothing to do here
  }

  @Override
  public void applyHandler(MethodSpec.Builder methodBuilder) {
    // nothing to do here
  }

  @Override
  public BpmnElement getElement() {
    return element;
  }

  @Override
  public CodeBlock getHandler() {
    throw new UnsupportedOperationException();
  }

  @Override
  public TypeName getHandlerType() {
    return OTHER;
  }

  @Override
  public String getLiteral() {
    return literal;
  }

  @Override
  public void hasPassed(MethodSpec.Builder methodBuilder) {
    methodBuilder.addStatement("instance.hasPassed(processInstanceKey, $S)", element.getId());
  }

  @Override
  public void initHandler(MethodSpec.Builder methodBuilder) {
    // nothing to do here
  }

  @Override
  public void initHandlerElement(MethodSpec.Builder methodBuilder) {
    // nothing to do here
  }

  @Override
  public CodeBlock initHandlerStatement() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void isWaitingAt(MethodSpec.Builder methodBuilder) {
    methodBuilder.addStatement("instance.isWaitingAt(processInstanceKey, $S)", element.getId());
  }
}
