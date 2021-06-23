package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.CALL_ACTIVITY_RULE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_RULE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_INSTANCE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TYPE_ABSTRACT_TEST_CASE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TYPE_BPMNDT_CONFIGURATION;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TYPE_CALL_ACTIVITY_RULE;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class AbstractTestCase implements Function<GeneratorContext, TypeSpec> {

  @Override
  public TypeSpec apply(GeneratorContext context) {
    FieldSpec processEngineRule;
    if (context.isSpringEnabled()) {
      processEngineRule = buildProcessEngineRuleFieldSpringEnabled();
    } else {
      processEngineRule = buildProcessEngineRuleField();
    }

    TypeName callActivityRuleType = ClassName.get(context.getPackageName(), TYPE_CALL_ACTIVITY_RULE);

    FieldSpec callActivityRule = FieldSpec.builder(callActivityRuleType, CALL_ACTIVITY_RULE, Modifier.PUBLIC)
        .addAnnotation(Rule.class)
        .initializer("new $T()", callActivityRuleType)
        .build();

    FieldSpec processInstance = FieldSpec.builder(ProcessInstance.class, PROCESS_INSTANCE, Modifier.PROTECTED).build();

    TypeSpec.Builder builder = TypeSpec.classBuilder(TYPE_ABSTRACT_TEST_CASE);
    
    if (context.isSpringEnabled()) {
      builder.addAnnotation(buildRunWithSpringRunnerAnnotation());
      builder.addAnnotation(buildContextConfigurationAnnotation(context));
    }

    builder.addModifiers(Modifier.ABSTRACT);
    builder.addField(processEngineRule);
    builder.addField(callActivityRule);
    builder.addField(processInstance);

    if (!context.isSpringEnabled()) {
      builder.addMethod(new BuildProcessEngine().apply(context));
      builder.addMethod(new BuildProcessEngineConfiguration().apply(context));
    }

    builder.addMethod(new AssertThatPi().get());
    builder.addMethod(new FindEventSubscription().get());

    return builder.build();
  }

  protected AnnotationSpec buildContextConfigurationAnnotation(GeneratorContext context) {
    TypeName bpmndtConfiguration = context.getTypeName(TYPE_BPMNDT_CONFIGURATION);
    return AnnotationSpec.builder(ContextConfiguration.class).addMember("classes", "$T.class", bpmndtConfiguration).build();
  }

  protected FieldSpec buildProcessEngineRuleField() {
    return FieldSpec.builder(ProcessEngineRule.class, PROCESS_ENGINE_RULE, Modifier.PUBLIC)
        .addAnnotation(Rule.class)
        .initializer("new $T(buildProcessEngine(), true)", ProcessEngineRule.class)
        .build();
  }

  /**
   * Builds an autowired {@link ProcessEngineRule} field, that must be provided via Spring
   * configuration.
   * 
   * @return The process engine rule field in case of a Spring enabled testing.
   */
  protected FieldSpec buildProcessEngineRuleFieldSpringEnabled() {
    return FieldSpec.builder(ProcessEngineRule.class, PROCESS_ENGINE_RULE, Modifier.PUBLIC)
        .addAnnotation(Autowired.class)
        .addAnnotation(Rule.class)
        .build();
  }

  protected AnnotationSpec buildRunWithSpringRunnerAnnotation() {
    return AnnotationSpec.builder(RunWith.class).addMember("value", "$T.class", SpringRunner.class).build();
  }
}
