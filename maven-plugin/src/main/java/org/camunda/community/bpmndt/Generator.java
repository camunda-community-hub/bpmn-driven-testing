package org.camunda.community.bpmndt;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.apache.maven.plugin.logging.Log;
import org.camunda.community.bpmndt.api.AbstractJUnit4TestCase;
import org.camunda.community.bpmndt.api.AbstractJUnit5TestCase;
import org.camunda.community.bpmndt.api.AbstractTestCase;
import org.camunda.community.bpmndt.api.CallActivityDefinition;
import org.camunda.community.bpmndt.api.CallActivityHandler;
import org.camunda.community.bpmndt.api.EventHandler;
import org.camunda.community.bpmndt.api.ExternalTaskHandler;
import org.camunda.community.bpmndt.api.JobHandler;
import org.camunda.community.bpmndt.api.MultiInstanceHandler;
import org.camunda.community.bpmndt.api.MultiInstanceScopeHandler;
import org.camunda.community.bpmndt.api.TestCaseExecutor;
import org.camunda.community.bpmndt.api.TestCaseInstance;
import org.camunda.community.bpmndt.api.UserTaskHandler;
import org.camunda.community.bpmndt.api.cfg.BpmndtParseListener;
import org.camunda.community.bpmndt.api.cfg.BpmndtProcessEnginePlugin;
import org.camunda.community.bpmndt.api.cfg.SpringConfiguration;
import org.camunda.community.bpmndt.cmd.BuildTestCaseContext;
import org.camunda.community.bpmndt.cmd.CollectBpmnFiles;
import org.camunda.community.bpmndt.cmd.DeleteTestSources;
import org.camunda.community.bpmndt.cmd.GenerateMultiInstanceHandler;
import org.camunda.community.bpmndt.cmd.GenerateMultiInstanceScopeHandler;
import org.camunda.community.bpmndt.cmd.GenerateSpringConfiguration;
import org.camunda.community.bpmndt.cmd.GenerateTestCase;
import org.camunda.community.bpmndt.cmd.WriteJavaFile;
import org.camunda.community.bpmndt.cmd.WriteJavaType;
import org.camunda.community.bpmndt.model.TestCase;

/**
 * Class that is responsible for generating test code and writing the generated files to the test
 * source directory.
 */
public class Generator {

  private final Log log;

  private final GeneratorResult result;

  public Generator(Log log) {
    this.log = log;

    result = new GeneratorResult();
  }

  public void generate(GeneratorContext ctx) {
    result.clear();

    // delete previously generated source files
    new DeleteTestSources().apply(ctx);

    // collect BPMN files
    Collection<Path> bpmnFiles = new CollectBpmnFiles().apply(ctx.getMainResourcePath());
    for (Path bpmnFile : bpmnFiles) {
      String relativePath = ctx.getMainResourcePath().relativize(bpmnFile).toString().replace('\\', '/');
      log.info(String.format("Found BPMN file: %s", relativePath));
    }

    // generate test cases for each BPMN file
    for (Path bpmnFile : bpmnFiles) {
      log.info("");

      generateTestCases(ctx, bpmnFile);
    }

    // generate Spring configuration
    if (ctx.isSpringEnabled()) {
      log.info("");

      generateSpringConfiguration(ctx);
    }

    log.info("");

    WriteJavaFile writeJavaFile = new WriteJavaFile(log, ctx);

    // write test cases
    log.info("Writing test cases");
    result.getFiles().forEach(writeJavaFile);

    if (!result.getAdditionalFiles().isEmpty()) {
      log.info("");

      // write additional classes
      log.info("Writing additional classes");
      result.getAdditionalFiles().forEach(writeJavaFile);
    }

    log.info("");

    Set<Class<?>> apiClasses = new TreeSet<>(Comparator.comparing(Class::getName));

    apiClasses.add(AbstractTestCase.class);
    apiClasses.add(CallActivityDefinition.class);
    apiClasses.add(CallActivityHandler.class);
    apiClasses.add(ExternalTaskHandler.class);
    apiClasses.add(EventHandler.class);
    apiClasses.add(JobHandler.class);
    apiClasses.add(MultiInstanceHandler.class);
    apiClasses.add(MultiInstanceScopeHandler.class);
    apiClasses.add(TestCaseInstance.class);
    apiClasses.add(TestCaseExecutor.class);
    apiClasses.add(UserTaskHandler.class);

    apiClasses.add(BpmndtParseListener.class);
    apiClasses.add(BpmndtProcessEnginePlugin.class);

    if (ctx.isJUnit5Enabled()) {
      apiClasses.add(AbstractJUnit5TestCase.class);
    } else {
      apiClasses.add(AbstractJUnit4TestCase.class);
    }

    if (ctx.isSpringEnabled()) {
      apiClasses.add(SpringConfiguration.class);
    }

    WriteJavaType writeJavaType = new WriteJavaType(log, ctx);

    // write API classes
    log.info("Writing API classes");
    apiClasses.forEach(writeJavaType);
  }

  protected void generateSpringConfiguration(GeneratorContext ctx) {
    log.info("Generating Spring configuration");
    new GenerateSpringConfiguration(result).accept(ctx);
  }

  protected void generateMultiInstanceHandlers(TestCaseContext ctx) {
    ctx.getActivities(activity -> activity.isMultiInstance() && !activity.isScope()).forEach(new GenerateMultiInstanceHandler(result));
  }

  protected void generateMultiInstanceScopeHandlers(TestCaseContext ctx) {
    ctx.getActivities(activity -> activity.isMultiInstance() && activity.isScope()).forEach(new GenerateMultiInstanceScopeHandler(result));
  }

  protected void generateTestCases(GeneratorContext gCtx, Path bpmnFile) {
    BpmnSupport bpmnSupport = BpmnSupport.of(bpmnFile);
    log.info(String.format("Process: %s", bpmnSupport.getProcessId()));

    // get test cases from BPMN model
    List<TestCase> testCases = bpmnSupport.getTestCases();
    if (testCases.isEmpty()) {
      log.info("No test cases defined");
      return;
    }

    Consumer<TestCaseContext> generate = new GenerateTestCase(gCtx, result);

    BuildTestCaseContext ctxBuilder = new BuildTestCaseContext(gCtx, bpmnSupport);

    for (int i = 0; i < testCases.size(); i++) {
      TestCaseContext ctx = ctxBuilder.apply(testCases.get(i), i);

      String testCaseName = ctx.getName();

      // check for duplicate test case names
      if (ctx.hasDuplicateName()) {
        log.warn(String.format("Skipping test case '%s': Name must be unique", testCaseName));
        continue;
      }

      log.info(String.format("Generating test case '%s'", testCaseName));
      generate.accept(ctx);
      generateMultiInstanceHandlers(ctx);
      generateMultiInstanceScopeHandlers(ctx);
    }
  }

  public GeneratorResult getResult() {
    return result;
  }
}
