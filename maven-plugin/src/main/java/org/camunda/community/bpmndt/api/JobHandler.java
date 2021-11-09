package org.camunda.community.bpmndt.api;

import java.util.List;
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

  private final Cardinality cardinality;

  private BiConsumer<ProcessInstanceAssert, JobAssert> verifier;

  private Consumer<Job> action;

  public JobHandler(ProcessEngine processEngine, String activityId) {
    this(processEngine, activityId, Cardinality.ONE);
  }

  /**
   * Creates a new job handler. This constructor is used in the context of multi instance activities.
   * 
   * @param processEngine The used process engine.
   * 
   * @param activityId The ID of the related activity.
   * 
   * @param cardinality Expected job cardinality.
   */
  public JobHandler(ProcessEngine processEngine, String activityId, Cardinality cardinality) {
    this.processEngine = processEngine;
    this.activityId = activityId;
    this.cardinality = cardinality;

    action = this::execute;
  }

  protected void apply(ProcessInstance pi) {
    List<Job> jobs = ProcessEngineTests.jobQuery().processInstanceId(pi.getId()).activityId(activityId).list();

    if (cardinality == Cardinality.ONE && jobs.size() != 1) {
      throw new AssertionError(String.format("Expected exactly one job for activity '%s'", activityId));
    } else if (cardinality == Cardinality.ONE_TO_N && jobs.isEmpty()) {
      throw new AssertionError(String.format("Expected at least one job for activity '%s'", activityId));
    } else if (cardinality == Cardinality.ZERO_TO_ONE && jobs.size() > 1) {
      throw new AssertionError(String.format("Expected at most one job for activity '%s'", activityId));
    } else if ((cardinality == Cardinality.ZERO_TO_ONE || cardinality == Cardinality.ZERO_TO_N) && jobs.isEmpty()) {
      return;
    }
    
    Job job = jobs.get(0);

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
   * Executes a custom action that handles the job.
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

  /**
   * Possible job cardinalities.
   */
  public static enum Cardinality {

    /** 1..n - parallel multi instance async before. */
    ONE_TO_N,
    /** Default cardinality. */
    ONE,
    /** 0..n - parallel multi instance async after. */
    ZERO_TO_N,
    /** 0..1 - sequential multi instance async after. */
    ZERO_TO_ONE,
  }
}
