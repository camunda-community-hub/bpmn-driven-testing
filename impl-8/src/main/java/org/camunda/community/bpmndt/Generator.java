package org.camunda.community.bpmndt;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.camunda.community.bpmndt.api.AbstractJUnit5TestCase;
import org.camunda.community.bpmndt.api.AbstractTestCase;
import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.CustomMultiInstanceHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.MessageEventHandler;
import org.camunda.community.bpmndt.api.OutboundConnectorHandler;
import org.camunda.community.bpmndt.api.ReceiveTaskHandler;
import org.camunda.community.bpmndt.api.SignalEventHandler;
import org.camunda.community.bpmndt.api.TestCaseExecutor;
import org.camunda.community.bpmndt.api.TestCaseInstance;
import org.camunda.community.bpmndt.api.TestCaseInstanceElement;
import org.camunda.community.bpmndt.api.TestCaseInstanceMemo;
import org.camunda.community.bpmndt.api.TimerEventHandler;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.camunda.community.bpmndt.cmd.BuildTestCaseContext;
import org.camunda.community.bpmndt.cmd.CollectBpmnFiles;
import org.camunda.community.bpmndt.cmd.DeleteTestSources;
import org.camunda.community.bpmndt.cmd.GenerateSimulateSubProcessResource;
import org.camunda.community.bpmndt.cmd.GenerateTestCase;
import org.camunda.community.bpmndt.cmd.WriteJavaFile;
import org.camunda.community.bpmndt.cmd.WriteJavaType;
import org.camunda.community.bpmndt.model.TestCase;
import org.camunda.community.bpmndt.model.TestCases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that is responsible for generating test code and writing the generated files to the test source directory.
 */
public class Generator {

  private static final Logger LOGGER = LoggerFactory.getLogger(Generator.class);

  private final GeneratorResult result;

  public Generator() {
    result = new GeneratorResult();
  }

  public void generate(GeneratorContext ctx) {
    result.clear();

    // delete previously generated source files
    new DeleteTestSources().apply(ctx);

    // collect BPMN files
    var bpmnFiles = new CollectBpmnFiles().apply(ctx.getMainResourcePath());
    for (Path bpmnFile : bpmnFiles) {
      var relativePath = ctx.getMainResourcePath().relativize(bpmnFile).toString().replace('\\', '/');
      LOGGER.info("Found BPMN file: {}", relativePath);
    }

    // generate test cases for each BPMN file
    for (Path bpmnFile : bpmnFiles) {
      LOGGER.info("");

      generateTestCases(ctx, bpmnFile);
    }

    // generate simulate sub process resource class
    new GenerateSimulateSubProcessResource(result).accept(ctx);

    LOGGER.info("");

    var writeJavaFile = new WriteJavaFile(ctx);

    // write test cases
    LOGGER.info("Writing test cases");
    result.getFiles().forEach(writeJavaFile);

    if (!result.getAdditionalFiles().isEmpty()) {
      LOGGER.info("");

      // write additional classes
      LOGGER.info("Writing additional classes");
      result.getAdditionalFiles().forEach(writeJavaFile);
    }

    LOGGER.info("");

    var apiClasses = new TreeSet<Class<?>>(Comparator.comparing(Class::getName));

    apiClasses.add(AbstractJUnit5TestCase.class);
    apiClasses.add(AbstractTestCase.class);
    apiClasses.add(CallActivityHandler.class);
    apiClasses.add(CustomMultiInstanceHandler.class);
    apiClasses.add(JobHandler.class);
    apiClasses.add(MessageEventHandler.class);
    apiClasses.add(OutboundConnectorHandler.class);
    apiClasses.add(ReceiveTaskHandler.class);
    apiClasses.add(SignalEventHandler.class);
    apiClasses.add(TestCaseExecutor.class);
    apiClasses.add(TestCaseInstance.class);
    apiClasses.add(TestCaseInstanceElement.class);
    apiClasses.add(TestCaseInstanceMemo.class);
    apiClasses.add(TimerEventHandler.class);
    apiClasses.add(UserTaskHandler.class);

    var writeJavaType = new WriteJavaType(ctx);

    // write API classes
    LOGGER.info("Writing API classes");
    apiClasses.forEach(writeJavaType);
  }

  protected void generateTestCases(GeneratorContext gCtx, Path bpmnFile) {
    // get test cases from BPMN model
    var testCases = TestCases.of(bpmnFile);
    if (!testCases.isPlatform8()) {
      var relativePath = gCtx.getMainResourcePath().relativize(bpmnFile).toString().replace('\\', '/');
      LOGGER.warn("Skipping BPMN model {}, since it is not designed for Camunda Platform 8", relativePath);
      return;
    }

    for (String processId : testCases.getProcessIds()) {
      LOGGER.info("Process: {}", processId);

      generateTestCases(gCtx, bpmnFile, testCases.get(processId));
    }
  }

  protected void generateTestCases(GeneratorContext gCtx, Path bpmnFile, List<TestCase> testCases) {
    if (testCases.isEmpty()) {
      LOGGER.info("No test cases defined");
      return;
    }

    var generate = new GenerateTestCase(result);

    var buildTestCaseContext = new BuildTestCaseContext(gCtx, bpmnFile);
    for (int i = 0; i < testCases.size(); i++) {
      var testCase = testCases.get(i);

      // check for invalid test cases
      if (testCase.hasEmptyPath()) {
        LOGGER.error("Test case #{} has an empty path", i + 1);
        continue;
      }
      if (testCase.hasIncompletePath()) {
        LOGGER.error("Test case #{} has an incomplete path", i + 1);
        continue;
      }
      if (testCase.hasInvalidPath()) {
        LOGGER.error("Test case #{} has an invalid path - invalid element IDs: {}", i + 1, testCase.getInvalidElementIds());
        continue;
      }

      var ctx = buildTestCaseContext.apply(testCases.get(i));

      // check for duplicate test case names
      if (ctx.hasDuplicateName()) {
        LOGGER.warn("Skipping test case #{}: name '{}' must be unique", i + 1, ctx.getName());
        continue;
      }

      LOGGER.info("Generating test case '{}'", ctx.getName());
      generate.accept(ctx);
    }
  }

  public GeneratorResult getResult() {
    return result;
  }
}
