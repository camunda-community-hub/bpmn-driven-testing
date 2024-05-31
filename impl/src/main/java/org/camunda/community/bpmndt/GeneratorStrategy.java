package org.camunda.community.bpmndt;

import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.model.TestCaseActivity;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Strategy, used per activity when generating a test case.
 */
public interface GeneratorStrategy {

  /**
   * Adds a handler field to the class, if the activity is handled by a handler - e.g.:
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
   * Adds a {@link JobHandler} field to the class, if an asynchronous continuation is configured after the activity - e.g.:
   *
   * <pre>
   * private JobHandler sendMailServiceTaskAfter;
   * </pre>
   *
   * @param classBuilder The class builder to use.
   */
  void addHandlerFieldAfter(TypeSpec.Builder classBuilder);

  /**
   * Adds a {@link JobHandler} field to the class, if an asynchronous continuation is configured before the activity - e.g.:
   *
   * <pre>
   * private JobHandler sendMailServiceTaskBefore;
   * </pre>
   *
   * @param classBuilder The class builder to use.
   */
  void addHandlerFieldBefore(TypeSpec.Builder classBuilder);

  /**
   * Adds a "handle" method to the class, if the activity is handled by a handler - e.g.:
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
   * Adds a method to the class, which provides a {@link JobHandler}, if an asynchronous continuation is configured after the activity - e.g.:
   *
   * <pre>
   * public JobHandler handleSendMailServiceTaskAfter() {
   *   return sendMailServiceTaskAfter;
   * }
   * </pre>
   *
   * @param classBuilder The class builder to use.
   */
  void addHandlerMethodAfter(TypeSpec.Builder classBuilder);

  /**
   * Adds a method to the class, which provides a {@link JobHandler}, if an asynchronous continuation is configured before the activity - e.g.:
   *
   * <pre>
   * public JobHandler handleSendMailServiceTaskBefore() {
   *   return sendMailServiceTaskBefore;
   * }
   * </pre>
   *
   * @param classBuilder The class builder to use.
   */
  void addHandlerMethodBefore(TypeSpec.Builder classBuilder);

  /**
   * Adds code to the execute/apply method, if the activity is handled by a handler and a wait state - e.g.:
   *
   * <pre>
   * assertThat(pi).isWaitingAt("placeOrderExternalTask");
   * instance.apply(placeOrderExternalTask)
   * </pre>
   * <p>
   * or the previous activity is an event based gateway.
   *
   * @param methodBuilder The method builder to use.
   */
  void applyHandler(MethodSpec.Builder methodBuilder);

  /**
   * Adds code to the execute/apply method, if an asynchronous continuation is configured after the activity - e.g.:
   *
   * <pre>
   * assertThat(pi).isWaitingAt("sendMailServiceTask");
   * instance.apply(sendMailServiceTaskAfter)
   * </pre>
   *
   * @param methodBuilder The method builder to use.
   */
  void applyHandlerAfter(MethodSpec.Builder methodBuilder);

  /**
   * Adds code to the execute/apply method, if an asynchronous continuation is configured before the activity - e.g.:
   *
   * <pre>
   * assertThat(pi).isWaitingAt("sendMailServiceTask");
   * instance.apply(sendMailServiceTaskBefore)
   * </pre>
   *
   * @param methodBuilder The method builder to use.
   */
  void applyHandlerBefore(MethodSpec.Builder methodBuilder);

  /**
   * Returns the underlying activity.
   *
   * @return The test case activity.
   */
  TestCaseActivity getActivity();

  /**
   * Returns code for getting a handler field reference. Normally a handler field is referenced using {@link TestCaseActivity#getId()} as literal, but in case
   * of a multi instance scope a specific {@code getHandler} method must be called.<br>
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
   * Returns code for getting an after handler field reference. Normally an after handler field is referenced using {@link TestCaseActivity#getId()} as literal
   * + {@code After}, but in case of a multi instance scope a specific {@code getHandler} method must be called.<br>
   * <br>
   * <p>
   * Normally:
   *
   * <pre>
   * approveUserTaskAfter
   * </pre>
   * <p>
   * Multi instance scope:
   *
   * <pre>
   * getApproveUserTaskHandlerAfter(loopIndex)
   * </pre>
   *
   * @return The after handler reference code.
   */
  CodeBlock getHandlerAfter();

  /**
   * Returns code for getting a before handler field reference. Normally a before handler field is referenced using {@link TestCaseActivity#getId()} as literal
   * + {@code Before}, but in case of a multi instance scope a specific {@code getHandler} method must be called.<br>
   * <br>
   * <p>
   * Normally:
   *
   * <pre>
   * approveUserTaskBefore
   * </pre>
   * <p>
   * Multi instance scope:
   *
   * <pre>
   * getApproveUserTaskHandlerBefore(loopIndex)
   * </pre>
   *
   * @return The before handler reference code.
   */
  CodeBlock getHandlerBefore();

  /**
   * Returns the type name of the related handler or {@code Void}, if the activity is not handled by a specific handler.
   *
   * @return The handler type name e.g. {@code TypeName.get(UserTaskHandler.class)}.
   */
  TypeName getHandlerType();

  /**
   * Gets the ID of the underlying activity as a literal.
   *
   * @return The activity ID literal.
   */
  String getLiteral();

  /**
   * Adds code, which asserts that the process instance has passed an activity.
   *
   * @param methodBuilder The method builder to use.
   */
  void hasPassed(MethodSpec.Builder methodBuilder);

  /**
   * Adds code, which initializes a handler field.
   *
   * <pre>
   * approveUserTask = new UserTaskHandler(getProcessEngine(), "approveUserTask");
   * </pre>
   *
   * @param methodBuilder The method builder to use.
   * @see #initHandlerStatement()
   */
  void initHandler(MethodSpec.Builder methodBuilder);

  /**
   * Adds code, which initializes an after handler field.
   *
   * <pre>
   * approveUserTaskAfter = new JobHandler(getProcessEngine(), "approveUserTask");
   * </pre>
   *
   * @param methodBuilder The method builder to use.
   * @see #initHandlerAfterStatement()
   */
  void initHandlerAfter(MethodSpec.Builder methodBuilder);

  /**
   * Returns the statement that initializes the after handler.
   *
   * <pre>
   * new JobHandler(getProcessEngine(), "approveUserTask");
   * </pre>
   *
   * @return The code.
   */
  CodeBlock initHandlerAfterStatement();

  /**
   * Adds code, which initializes a before handler field.
   *
   * <pre>
   * approveUserTaskBefore = new JobHandler(getProcessEngine(), "approveUserTask");
   * </pre>
   *
   * @param methodBuilder The method builder to use.
   * @see #initHandlerBeforeStatement()
   */
  void initHandlerBefore(MethodSpec.Builder methodBuilder);

  /**
   * Returns the statement that initializes the before handler.
   *
   * <pre>
   * new JobHandler(getProcessEngine(), "approveUserTask");
   * </pre>
   *
   * @return The code.
   */
  CodeBlock initHandlerBeforeStatement();

  /**
   * Returns the statement that initializes the handler.
   *
   * <pre>
   * new UserTaskHandler(getProcessEngine(), "approveUserTask");
   * </pre>
   *
   * @return The code.
   */
  CodeBlock initHandlerStatement();

  /**
   * Adds code, which asserts that the process instance is waiting at an activity.
   *
   * @param methodBuilder The method builder to use.
   */
  void isWaitingAt(MethodSpec.Builder methodBuilder);

  /**
   * Set the multi instance parent indicator.
   *
   * @param multiInstanceParent {@code true}, if the strategy is applied on a multi instance scope handler. Otherwise {@code} false.
   */
  void setMultiInstanceParent(boolean multiInstanceParent);

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
