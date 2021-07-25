package org.camunda.community.bpmndt.api;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.assertions.bpmn.JobAssert;
import org.camunda.bpm.engine.test.assertions.bpmn.ProcessInstanceAssert;

/**
 * Fluent API to handle jobs (asynchronous continuation and timer catch events).
 */
public class JobHandler {

  private final ProcessEngine processEngine;
  private final String activityId;

  private BiConsumer<ProcessInstanceAssert, JobAssert> verifier;

  private Consumer<Job> action;

  public JobHandler(ProcessEngine processEngine, String activityId) {
    this.processEngine = processEngine;
    this.activityId = activityId;

    action = this::execute;
  }

  protected void apply(ProcessInstance pi) {
    Job job = ProcessEngineTests.job(activityId, pi);

    if (verifier != null) {
      verifier.accept(ProcessEngineTests.assertThat(pi), ProcessEngineTests.assertThat(job));
    }

    action.accept(job);
  }

  /**
   * Executes the job with an action that calls {@code executeJob}.
   * 
   * @see ManagementService#executeJob(String)
   */
  public void execute() {
    action = this::execute;
  }

  /**
   * Executes the job with a custom action that is executed when the handler is applied.
   * 
   * @param action A specific action that accepts the related {@link Job}.
   */
  public void execute(Consumer<Job> action) {
    this.action = action;
  }

  protected void execute(Job job) {
    processEngine.getManagementService().executeJob(job.getId());
  }

  /**
   * Verifies the job's waiting state.
   * 
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} and an {@link JobAssert}
   *        instance.
   * 
   * @return The handler.
   */
  public JobHandler verify(BiConsumer<ProcessInstanceAssert, JobAssert> verifier) {
    this.verifier = verifier;
    return this;
  }
}
