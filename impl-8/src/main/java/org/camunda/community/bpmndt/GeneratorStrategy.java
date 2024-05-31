package org.camunda.community.bpmndt;

import org.camunda.community.bpmndt.model.BpmnElement;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Strategy, used per BPMN element when generating a test case.
 */
public interface GeneratorStrategy {

  /**
   * Adds a handler field to the class, if the element is handled by a handler - e.g.:
   *
   * <pre>
   * private UserTaskHandler approveUserTask;
   * </pre>
   * <p>
   * Otherwise, no field is added.
   *
   * @param classBuilder The class builder to use.
   */
  void addHandlerField(TypeSpec.Builder classBuilder);

  /**
   * Adds a "handle" method to the class, if the element is handled by a handler - e.g.:
   *
   * <pre>
   * public UserTaskHandler handleApproveUserTask() {
   *   return approveUserTask;
   * }
   * </pre>
   * <p>
   * Otherwise, no field is added.
   *
   * @param classBuilder The class builder to use.
   */
  void addHandlerMethod(TypeSpec.Builder classBuilder);

  /**
   * Adds code to the execute/apply method, if the element is handled by a handler - e.g.:
   *
   * <pre>
   * instance.isWaitingAt(processInstanceEvent, "placeOrderExternalTask");
   * instance.apply(processInstanceEvent, placeOrderExternalTask)
   * </pre>
   * <p>
   * or the previous element is an event based gateway.
   *
   * @param methodBuilder The method builder to use.
   */
  void applyHandler(MethodSpec.Builder methodBuilder);

  /**
   * Returns the underlying BPMN element.
   *
   * @return The test case element.
   */
  BpmnElement getElement();

  /**
   * Returns code for getting a handler field reference. Normally a handler field is references using {@link #getLiteral()}, but in case of a multi instance
   * scope a specific {@code getHandler} method must be called.<br>
   * <br>
   * <p>
   * Normally:
   *
   * <pre>
   * approveUserTask
   * </pre>
   * <p>
   * Multi instance scope:
   *
   * <pre>
   * getApproveUserTaskHandler(loopIndex)
   * </pre>
   *
   * @return The handler reference code.
   */
  CodeBlock getHandler();

  /**
   * Returns the type name of the related handler or {@code Void}, if the activity is not handled by a specific handler.
   *
   * @return The handler type name e.g. {@code TypeName.get(UserTaskHandler.class)}.
   */
  TypeName getHandlerType();

  /**
   * Gets the ID of the underlying BPMN element as a literal.
   *
   * @return The element ID literal.
   */
  String getLiteral();

  /**
   * Adds code, which asserts that the process instance has passed a BPMN element.
   *
   * @param methodBuilder The method builder to use.
   */
  void hasPassed(MethodSpec.Builder methodBuilder);

  /**
   * Adds code, which initializes a handler field.
   *
   * <pre>
   * approveUserTask = new UserTaskHandler(approveUserTaskElement);
   * </pre>
   *
   * @param methodBuilder The method builder to use.
   * @see #initHandlerStatement()
   */
  void initHandler(MethodSpec.Builder methodBuilder);

  /**
   * Adds code, which initializes the handler's element - e.g. {@link org.camunda.community.bpmndt.api.TestCaseInstanceElement.UserTaskElement}
   *
   * @param methodBuilder The method builder to use.
   */
  void initHandlerElement(MethodSpec.Builder methodBuilder);

  /**
   * Returns the statement that initializes the handler.
   *
   * <pre>
   * new UserTaskHandler(approveUserTaskElement)
   * </pre>
   *
   * @return The code.
   */
  CodeBlock initHandlerStatement();

  /**
   * Adds code, which asserts that the process instance is waiting at a BPMN element.
   *
   * @param methodBuilder The method builder to use.
   */
  void isWaitingAt(MethodSpec.Builder methodBuilder);
}
