package org.camunda.community.bpmndt.api;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.function.Consumer;

import org.camunda.community.bpmndt.api.TestCaseInstanceElement.TimerEventElement;
import org.camunda.community.bpmndt.api.TestCaseInstanceMemo.TimerMemo;

import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.assertions.ProcessInstanceAssert;

/**
 * Fluent API to handle timer catch events.
 */
public class TimerEventHandler {

  private final TimerEventElement element;

  private Consumer<ProcessInstanceAssert> verifier;

  private Consumer<String> timeDateExpressionConsumer;
  private Consumer<String> timeDurationExpressionConsumer;

  private Consumer<LocalDateTime> timeDateConsumer;
  private Consumer<Duration> timeDurationConsumer;

  public TimerEventHandler(String elementId) {
    if (elementId == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    element = new TimerEventElement();
    element.id = elementId;
  }

  public TimerEventHandler(TimerEventElement element) {
    if (element == null) {
      throw new IllegalArgumentException("element is null");
    }
    if (element.id == null) {
      throw new IllegalArgumentException("element ID is null");
    }

    this.element = element;
  }

  void apply(TestCaseInstance instance, long flowScopeKey) {
    if (verifier != null) {
      var processInstanceKey = instance.getProcessInstanceKey(flowScopeKey);
      verifier.accept(new ProcessInstanceAssert(processInstanceKey, BpmnAssert.getRecordStream()));
    }

    if (timeDateExpressionConsumer != null) {
      timeDateExpressionConsumer.accept(element.timeDate);
    }
    if (timeDurationExpressionConsumer != null) {
      timeDurationExpressionConsumer.accept(element.timeDuration);
    }

    var timer = getTimer(instance, flowScopeKey);

    if (timeDateConsumer != null) {
      var dueDateInstant = Instant.ofEpochMilli(timer.dueDate);
      var dueDate = LocalDateTime.ofInstant(dueDateInstant, ZoneId.systemDefault());
      timeDateConsumer.accept(dueDate);
    }
    if (timeDurationConsumer != null) {
      timeDurationConsumer.accept(toDuration(timer.dueDate, timer.creationDate));
    }

    instance.getEngine().increaseTime(Duration.ofMillis(timer.dueDate - System.currentTimeMillis()));
  }

  /**
   * Customizes the handler, using the given {@link Consumer} function. This method can be used to apply a common customization needed for different test
   * cases.
   *
   * <pre>
   * tc.handleTimerEvent().customize(this::prepare);
   * </pre>
   *
   * @param customizer A function that accepts a {@link TimerEventHandler}.
   * @return The handler.
   */
  public TimerEventHandler customize(Consumer<TimerEventHandler> customizer) {
    if (customizer != null) {
      customizer.accept(this);
    }
    return this;
  }

  /**
   * Verifies the user timer event's waiting state.
   *
   * @param verifier Verifier that accepts an {@link ProcessInstanceAssert} instance.
   * @return The handler.
   */
  public TimerEventHandler verify(Consumer<ProcessInstanceAssert> verifier) {
    this.verifier = verifier;
    return this;
  }

  /**
   * Verifies the timer event's due date, using a consumer.
   *
   * @param timeDateConsumer A consumer asserting the due date.
   * @return The handler.
   */
  public TimerEventHandler verifyTimeDate(Consumer<LocalDateTime> timeDateConsumer) {
    this.timeDateConsumer = timeDateConsumer;
    return this;
  }

  /**
   * Verifies that the timer event has a specific date constant or FEEL expression (see "Timer" section), using a consumer function.
   *
   * @param timeDateExpressionConsumer A consumer asserting the date constant or expression.
   * @return The handler.
   */
  public TimerEventHandler verifyTimeDateExpression(Consumer<String> timeDateExpressionConsumer) {
    this.timeDateExpressionConsumer = timeDateExpressionConsumer;
    return this;
  }

  /**
   * Verifies the timer event's calculated duration rounded to seconds, using a consumer.
   *
   * @param timeDurationConsumer A consumer asserting the duration.
   * @return The handler.
   */
  public TimerEventHandler verifyTimeDuration(Consumer<Duration> timeDurationConsumer) {
    this.timeDurationConsumer = timeDurationConsumer;
    return this;
  }

  /**
   * Verifies that the timer event has a specific duration constant or FEEL expression (see "Timer" section), using a consumer function.
   *
   * @param timeDurationExpressionConsumer A consumer asserting the duration constant or expression.
   * @return The handler.
   */
  public TimerEventHandler verifyTimeDurationExpression(Consumer<String> timeDurationExpressionConsumer) {
    this.timeDurationExpressionConsumer = timeDurationExpressionConsumer;
    return this;
  }

  /**
   * Calculates and rounds the duration up to the second.
   *
   * @param dueDate      The timer's due date.
   * @param creationDate The timer's creation date.
   * @return The duration rounded to seconds.
   */
  Duration toDuration(long dueDate, long creationDate) {
    long millis = Math.round((dueDate - creationDate + 999) / 1000 * 1000);
    return Duration.ofMillis(millis);
  }

  private TimerMemo getTimer(TestCaseInstance instance, long flowScopeKey) {
    var flowScopeKeys = instance.getKeys(flowScopeKey);

    return instance.select(memo -> {
      var timer = memo.timers.stream().filter(t ->
          flowScopeKeys.contains(t.flowScopeKey) && Objects.equals(t.elementId, element.id)
      ).findFirst();

      if (timer.isEmpty()) {
        var message = String.format("element %s of flow scope %d has no timer", element.id, flowScopeKey);
        throw instance.createException(message, flowScopeKey);
      }

      return timer.get();
    });
  }
}
