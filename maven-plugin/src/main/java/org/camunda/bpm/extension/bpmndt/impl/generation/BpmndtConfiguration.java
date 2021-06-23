package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_CONFIGURATION;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_NAME;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TYPE_BPMNDT_CONFIGURATION;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TYPE_BPMNDT_PLUGIN;

import java.util.Arrays;
import java.util.function.Function;

import javax.lang.model.element.Modifier;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.spring.SpringExpressionManager;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * {@link Configuration} class, that can be used via {@code @ContextConfiguration} when performing
 * Spring based testing.
 */
public class BpmndtConfiguration implements Function<GeneratorContext, TypeSpec> {

  @Override
  public TypeSpec apply(GeneratorContext context) {
    FieldSpec applicationContext = FieldSpec.builder(ApplicationContext.class, "applicationContext", Modifier.PRIVATE)
        .addAnnotation(Autowired.class)
        .build();

    FieldSpec dataSource = FieldSpec.builder(DataSource.class, "dataSource", Modifier.PRIVATE)
        .addAnnotation(AnnotationSpec.builder(Autowired.class).addMember("required", "false").build())
        .build();

    FieldSpec transactionManager = FieldSpec.builder(PlatformTransactionManager.class, "transactionManager", Modifier.PRIVATE)
        .addAnnotation(AnnotationSpec.builder(Autowired.class).addMember("required", "false").build())
        .build();

    Object[] newArgs = new Object[3];
    newArgs[0] = SpringProcessEngineConfiguration.class;
    newArgs[1] = PROCESS_ENGINE_CONFIGURATION;
    newArgs[2] = SpringProcessEngineConfiguration.class;

    Object[] expressionManagerArgs = new Object[2];
    expressionManagerArgs[0] = PROCESS_ENGINE_CONFIGURATION;
    expressionManagerArgs[1] = SpringExpressionManager.class;

    Object[] pluginsArgs = new Object[4];
    pluginsArgs[0] = PROCESS_ENGINE_CONFIGURATION;
    pluginsArgs[1] = Arrays.class;
    pluginsArgs[2] = SpinProcessEnginePlugin.class;
    pluginsArgs[3] = context.getTypeName(TYPE_BPMNDT_PLUGIN);

    MethodSpec afterPropertiesSet = MethodSpec.methodBuilder("afterPropertiesSet")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addException(Exception.class)
        .addStatement("$T dataSource = initDataSource()", DataSource.class)
        .addStatement("$T transactionManager = initTransactionManager(dataSource)", PlatformTransactionManager.class)
        .addCode("\n")
        .addStatement("$T $L = new $T()", newArgs)
        .addStatement("$L.setApplicationContext(applicationContext)", PROCESS_ENGINE_CONFIGURATION)
        .addStatement("$L.setDataSource(dataSource)", PROCESS_ENGINE_CONFIGURATION)
        .addStatement("$L.setExpressionManager(new $T(applicationContext, null))", expressionManagerArgs)
        .addStatement("$L.setProcessEngineName($S)", PROCESS_ENGINE_CONFIGURATION, PROCESS_ENGINE_NAME)
        .addStatement("$L.setProcessEnginePlugins($T.asList(new $T(), new $T()))", pluginsArgs)
        .addStatement("$L.setTransactionManager(transactionManager)", PROCESS_ENGINE_CONFIGURATION)
        .addCode("\n")
        .addStatement("$L = $L.buildProcessEngine()", PROCESS_ENGINE, PROCESS_ENGINE_CONFIGURATION)
        .build();

    MethodSpec initDataSource = MethodSpec.methodBuilder("initDataSource")
        .addModifiers(Modifier.PROTECTED)
        .returns(DataSource.class)
        .beginControlFlow("if (dataSource != null)")
        .addStatement("return dataSource")
        .endControlFlow()
        .addCode("\n")
        .addStatement("$T dataSource = new $T()", BasicDataSource.class, BasicDataSource.class)
        .addStatement("dataSource.setDriverClassName($S)", "org.h2.Driver")
        .addStatement("dataSource.setUrl($S)", "jdbc:h2:mem:bpmndt;DB_CLOSE_ON_EXIT=FALSE")
        .addCode("\n")
        .addStatement("return dataSource")
        .build();

    MethodSpec initTransactionManager = MethodSpec.methodBuilder("initTransactionManager")
        .addModifiers(Modifier.PROTECTED)
        .returns(PlatformTransactionManager.class)
        .addParameter(DataSource.class, "dataSource")
        .beginControlFlow("if (transactionManager != null)")
        .addStatement("return transactionManager")
        .endControlFlow()
        .addCode("\n")
        .addStatement("return new $T(dataSource)", DataSourceTransactionManager.class)
        .build();

    MethodSpec getProcessEngine = MethodSpec.methodBuilder("getProcessEngine")
        .addAnnotation(Bean.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(ProcessEngine.class)
        .addStatement("return processEngine")
        .build();

    MethodSpec getProcessEngineRule = MethodSpec.methodBuilder("getProcessEngineRule")
        .addAnnotation(Bean.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(ProcessEngineRule.class)
        .addStatement("return new $T(processEngine, true)", ProcessEngineRule.class)
        .build();

    return TypeSpec.classBuilder(TYPE_BPMNDT_CONFIGURATION)
        .addAnnotation(Configuration.class)
        .addSuperinterface(InitializingBean.class)
        .addModifiers(Modifier.PUBLIC)
        .addField(applicationContext)
        .addField(dataSource)
        .addField(transactionManager)
        .addField(ProcessEngine.class, PROCESS_ENGINE, Modifier.PRIVATE)
        .addMethod(afterPropertiesSet)
        .addMethod(initDataSource)
        .addMethod(initTransactionManager)
        .addMethod(getProcessEngine)
        .addMethod(getProcessEngineRule)
        .build();
  }
}
