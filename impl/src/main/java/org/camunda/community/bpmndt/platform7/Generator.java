package org.camunda.community.bpmndt.platform7;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.camunda.community.bpmndt.GeneratorResult;
import org.camunda.community.bpmndt.cmd.CollectBpmnFiles;
import org.camunda.community.bpmndt.cmd.DeleteTestSources;
import org.camunda.community.bpmndt.cmd.WriteJavaFile;
import org.camunda.community.bpmndt.cmd.WriteJavaType;
import org.camunda.community.bpmndt.model.platform7.TestCase;
import org.camunda.community.bpmndt.model.platform7.TestCases;
import org.camunda.community.bpmndt.platform7.api.AbstractJUnit5TestCase;
import org.camunda.community.bpmndt.platform7.api.AbstractTestCase;
import org.camunda.community.bpmndt.platform7.api.CallActivityDefinition;
import org.camunda.community.bpmndt.platform7.api.CallActivityHandler;
import org.camunda.community.bpmndt.platform7.api.EventHandler;
import org.camunda.community.bpmndt.platform7.api.ExternalTaskHandler;
import org.camunda.community.bpmndt.platform7.api.JobHandler;
import org.camunda.community.bpmndt.platform7.api.MultiInstanceHandler;
import org.camunda.community.bpmndt.platform7.api.MultiInstanceScopeHandler;
import org.camunda.community.bpmndt.platform7.api.TestCaseExecutor;
import org.camunda.community.bpmndt.platform7.api.TestCaseInstance;
import org.camunda.community.bpmndt.platform7.api.UserTaskHandler;
import org.camunda.community.bpmndt.platform7.api.cfg.BpmndtParseListener;
import org.camunda.community.bpmndt.platform7.api.cfg.BpmndtProcessEnginePlugin;
import org.camunda.community.bpmndt.platform7.api.cfg.SpringConfiguration;
import org.camunda.community.bpmndt.platform7.cmd.BuildTestCaseContext;
import org.camunda.community.bpmndt.platform7.cmd.GenerateMultiInstanceHandler;
import org.camunda.community.bpmndt.platform7.cmd.GenerateMultiInstanceScopeHandler;
import org.camunda.community.bpmndt.platform7.cmd.GenerateSpringConfiguration;
import org.camunda.community.bpmndt.platform7.cmd.GenerateTestCase;
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
    Collection<Path> bpmnFiles = new CollectBpmnFiles().apply(ctx);
    for (Path bpmnFile : bpmnFiles) {
      String relativePath = ctx.getMainResourcePath().relativize(bpmnFile).toString().replace('\\', '/');
      LOGGER.info("Found BPMN file: {}", relativePath);
    }

    // generate test cases for each BPMN file
    for (Path bpmnFile : bpmnFiles) {
      LOGGER.info("");

      generateTestCases(ctx, bpmnFile);
    }

    // generate Spring configuration
    if (ctx.isSpringEnabled()) {
      LOGGER.info("");

      generateSpringConfiguration(ctx);
    }

    LOGGER.info("");

    WriteJavaFile writeJavaFile = new WriteJavaFile(ctx);

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

    Set<Class<?>> apiClasses = new TreeSet<>(Comparator.comparing(Class::getName));

    apiClasses.add(AbstractJUnit5TestCase.class);
    apiClasses.add(AbstractTestCase.class);
    apiClasses.add(CallActivityDefinition.class);
    apiClasses.add(CallActivityHandler.class);
    apiClasses.add(ExternalTaskHandler.class);
    apiClasses.add(EventHandler.class);
    apiClasses.add(JobHandler.class);
    apiClasses.add(MultiInstanceHandler.class);
    apiClasses.add(MultiInstanceScopeHandler.class);
    apiClasses.add(TestCaseExecutor.class);
    apiClasses.add(TestCaseInstance.class);
    apiClasses.add(UserTaskHandler.class);

    apiClasses.add(BpmndtParseListener.class);
    apiClasses.add(BpmndtProcessEnginePlugin.class);

    if (ctx.isSpringEnabled()) {
      apiClasses.add(SpringConfiguration.class);
    }

    WriteJavaType writeJavaType = new WriteJavaType(ctx);

    // write API classes
    LOGGER.info("Writing API classes");
    apiClasses.forEach(writeJavaType);
  }

  protected void generateMultiInstanceHandlers(TestCaseContext ctx) {
    GenerateMultiInstanceHandler generate = new GenerateMultiInstanceHandler(ctx, result);
    ctx.getMultiInstanceActivities().forEach(generate);
  }

  protected void generateMultiInstanceScopeHandlers(TestCaseContext ctx) {
    GenerateMultiInstanceScopeHandler generate = new GenerateMultiInstanceScopeHandler(ctx, result);
    ctx.getMultiInstanceScopes().forEach(generate);
  }

  protected void generateSpringConfiguration(GeneratorContext ctx) {
    LOGGER.info("Generating Spring configuration");
    new GenerateSpringConfiguration(result).accept(ctx);
  }

  protected void generateTestCases(GeneratorContext gCtx, Path bpmnFile) {
    // get test cases from BPMN model
    TestCases testCases = TestCases.of(bpmnFile);

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

    GenerateTestCase generate = new GenerateTestCase(gCtx, result);

    BuildTestCaseContext buildTestCaseContext = new BuildTestCaseContext(gCtx, bpmnFile);
    for (int i = 0; i < testCases.size(); i++) {
      TestCase testCase = testCases.get(i);

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
        LOGGER.error("Test case #{} has an invalid path - invalid flow node IDs: {}", i + 1, testCase.getInvalidFlowNodeIds());
        continue;
      }

      TestCaseContext ctx = buildTestCaseContext.apply(testCases.get(i));

      // check for duplicate test case names
      if (ctx.hasDuplicateName()) {
        LOGGER.warn("Skipping test case #{}: Name '{}' must be unique", i + 1, ctx.getName());
        continue;
      }

      LOGGER.info("Generating test case '{}'", ctx.getName());
      generate.accept(ctx);
      generateMultiInstanceHandlers(ctx);
      generateMultiInstanceScopeHandlers(ctx);
    }
  }

  public GeneratorResult getResult() {
    return result;
  }
}
