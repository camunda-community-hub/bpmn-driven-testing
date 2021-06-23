package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_CONFIGURATION;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TYPE_BPMNDT_PLUGIN;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TYPE_CALL_ACTIVITY_PARSE_LISTENER;

import java.util.LinkedList;
import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates a custom {@link ProcessEnginePlugin} that is used to configure a process engine to be
 * able to run generated test cases.
 */
public class BpmndtPlugin implements Function<GeneratorContext, TypeSpec> {

  @Override
  public TypeSpec apply(GeneratorContext context) {
    TypeName postBPMNParseListeners = ParameterizedTypeName.get(LinkedList.class, BpmnParseListener.class);
    TypeName callActivityParseListener = ClassName.get(context.getPackageName(), TYPE_CALL_ACTIVITY_PARSE_LISTENER);

    Object[] databaseSchemaUpdateArgs = new Object[2];
    databaseSchemaUpdateArgs[0] = PROCESS_ENGINE_CONFIGURATION;
    databaseSchemaUpdateArgs[1] = ProcessEngineConfigurationImpl.class;

    MethodSpec.Builder preInitBuilder = MethodSpec.methodBuilder("preInit")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ProcessEngineConfigurationImpl.class, PROCESS_ENGINE_CONFIGURATION)
        .addStatement("$T postBPMNParseListeners = new $T();", postBPMNParseListeners, postBPMNParseListeners)
        .addStatement("postBPMNParseListeners.add(new $T())", callActivityParseListener)
        .addCode("\n")
        .addStatement("$L.setCmmnEnabled(false)", PROCESS_ENGINE_CONFIGURATION)
        .addStatement("$L.setCustomPostBPMNParseListeners(postBPMNParseListeners)", PROCESS_ENGINE_CONFIGURATION)
        .addStatement("$L.setDatabaseSchemaUpdate($T.DB_SCHEMA_UPDATE_CREATE_DROP)", databaseSchemaUpdateArgs)
        .addStatement("$L.setHistoryLevel($T.HISTORY_LEVEL_FULL)", PROCESS_ENGINE_CONFIGURATION, HistoryLevel.class)
        .addStatement("$L.setInitializeTelemetry(false)", PROCESS_ENGINE_CONFIGURATION);
    
    if (!context.isSpringEnabled()) {
      // if Spring is not enabled, a custom JDBC url is set
      // otherwise a data source is used
      preInitBuilder.addStatement("$L.setJdbcUrl($S)", PROCESS_ENGINE_CONFIGURATION, "jdbc:h2:mem:bpmndt");
    }
    
    MethodSpec preInit = preInitBuilder
        .addStatement("$L.setJobExecutorActivate(false)", PROCESS_ENGINE_CONFIGURATION)
        .addStatement("$L.setMetricsEnabled(false)", PROCESS_ENGINE_CONFIGURATION)
        .build();

    return TypeSpec.classBuilder(TYPE_BPMNDT_PLUGIN)
        .addModifiers(Modifier.PUBLIC)
        .superclass(AbstractProcessEnginePlugin.class)
        .addMethod(preInit)
        .build();
  }
}
