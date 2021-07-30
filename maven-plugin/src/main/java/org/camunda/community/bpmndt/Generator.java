package org.camunda.community.bpmndt;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import org.apache.maven.plugin.logging.Log;
import org.camunda.community.bpmndt.api.AbstractJUnit4SpringBasedTestRule;
import org.camunda.community.bpmndt.api.AbstractJUnit4TestRule;
import org.camunda.community.bpmndt.api.CallActivityDefinition;
import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.ExternalTaskHandler;
import org.camunda.community.bpmndt.api.IntermediateCatchEventHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.TestCaseExecutor;
import org.camunda.community.bpmndt.api.TestCaseInstance;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.camunda.community.bpmndt.api.cfg.AbstractConfiguration;
import org.camunda.community.bpmndt.api.cfg.BpmndtCallActivityBehavior;
import org.camunda.community.bpmndt.api.cfg.BpmndtParseListener;
import org.camunda.community.bpmndt.api.cfg.BpmndtProcessEnginePlugin;
import org.camunda.community.bpmndt.cmd.BuildTestCaseContext;
import org.camunda.community.bpmndt.cmd.CollectBpmnFiles;
import org.camunda.community.bpmndt.cmd.DeleteTestSources;
import org.camunda.community.bpmndt.cmd.GenerateJUnit4TestRule;
import org.camunda.community.bpmndt.cmd.GenerateSpringConfiguration;
import org.camunda.community.bpmndt.cmd.WriteJavaFile;
import org.camunda.community.bpmndt.cmd.WriteJavaType;
import org.camunda.community.bpmndt.model.TestCase;

/**
 * Class that is responsible for generating test code and writing the generated files to the test
 * source directory.
 */
public class Generator {

  private final Log log;

  public Generator(Log log) {
    this.log = log;
  }

  public void generate(GeneratorContext ctx) {
    // delete previously generated source files
    new DeleteTestSources().apply(ctx);

    // collect BPMN files
    Collection<Path> bpmnFiles = new CollectBpmnFiles().apply(ctx.getMainResourcePath());
    for (Path bpmnFile : bpmnFiles) {
      String relativePath = ctx.getMainResourcePath().relativize(bpmnFile).toString().replace('\\', '/');
      log.info(String.format("Found BPMN file: %s", relativePath));
    }

    GeneratorResult result = new GeneratorResult();

    // generate test cases for each BPMN file
    for (Path bpmnFile : bpmnFiles) {
      log.info("");

      generateTestCases(ctx, result, bpmnFile);
    }

    // generate Spring configuration
    if (ctx.isSpringEnabled()) {
      log.info("");

      generateSpringConfiguration(ctx, result);
    }

    log.info("");

    WriteJavaFile write = new WriteJavaFile(log, ctx);

    // write test cases
    log.info("Writing test cases");
    result.getFiles().forEach(write);

    if (!result.getAdditionalFiles().isEmpty()) {
      log.info("");

      // write additional classes
      log.info("Writing additional classes");
      result.getAdditionalFiles().forEach(write);
    }

    log.info("");

    Set<Class<?>> apiClasses = new TreeSet<>(Comparator.comparing(Class::getName));
    apiClasses.add(AbstractJUnit4TestRule.class);
    apiClasses.add(CallActivityDefinition.class);
    apiClasses.add(CallActivityHandler.class);
    apiClasses.add(ExternalTaskHandler.class);
    apiClasses.add(IntermediateCatchEventHandler.class);
    apiClasses.add(JobHandler.class);
    apiClasses.add(TestCaseInstance.class);
    apiClasses.add(TestCaseExecutor.class);
    apiClasses.add(UserTaskHandler.class);

    apiClasses.add(BpmndtCallActivityBehavior.class);
    apiClasses.add(BpmndtParseListener.class);
    apiClasses.add(BpmndtProcessEnginePlugin.class);

    if (ctx.isSpringEnabled()) {
      apiClasses.add(AbstractJUnit4SpringBasedTestRule.class);
      apiClasses.add(AbstractConfiguration.class);
    }

    WriteJavaType writeType = new WriteJavaType(log, ctx);

    // write API classes
    log.info("Writing API classes");
    apiClasses.forEach(writeType);
  }

  protected void generateSpringConfiguration(GeneratorContext ctx, GeneratorResult result) {
    log.info("Generating Spring configuration");
    new GenerateSpringConfiguration(result).accept(ctx);
  }

  protected void generateTestCases(GeneratorContext ctx, GeneratorResult result, Path bpmnFile) {
    BpmnSupport bpmnSupport = BpmnSupport.of(bpmnFile);
    log.info(String.format("Process: %s", bpmnSupport.getProcessId()));

    // get test cases from BPMN model
    List<TestCase> testCases = bpmnSupport.getTestCases();
    if (testCases.isEmpty()) {
      log.info("No test cases defined");
      return;
    }

    BiConsumer<GeneratorContext, TestCaseContext> generate = new GenerateJUnit4TestRule(result);

    BuildTestCaseContext ctxBuilder = new BuildTestCaseContext(bpmnSupport);
    for (TestCase testCase : bpmnSupport.getTestCases()) {
      TestCaseContext testCaseContext = ctxBuilder.apply(testCase);

      String testCaseName = testCaseContext.getName();

      // check for duplicate test case names
      if (testCaseContext.hasDuplicateName()) {
        log.warn(String.format("Skipping test case '%s': Name must be unique", testCaseName));
        continue;
      }

      log.info(String.format("Generating test case '%s'", testCaseName));
      generate.accept(ctx, testCaseContext);
    }
  }
}
