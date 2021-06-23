package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.PROCESS_ENGINE_RULE;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TYPE_CALL_ACTIVITY_PARSE_LISTENER;
import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TYPE_CALL_ACTIVITY_RULE;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement.CallableElementBinding;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class CallActivityRule implements Function<GeneratorContext, TypeSpec> {

  @Override
  public TypeSpec apply(GeneratorContext context) {
    TypeName consumerI = ParameterizedTypeName.get(Consumer.class, VariableScope.class);
    TypeName consumerO = ParameterizedTypeName.get(Consumer.class, DelegateExecution.class);

    TypeName callbackI = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), consumerI);
    TypeName callbackO = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), consumerO);

    TypeName callActivityParseListener = context.getTypeName(TYPE_CALL_ACTIVITY_PARSE_LISTENER);

    MethodSpec constructor = MethodSpec.constructorBuilder()
        .addCode("\n")
        .addStatement("callbackI = new $T<>()", HashMap.class)
        .addStatement("callbackO = new $T<>()", HashMap.class)
        .build();
    
    MethodSpec register = MethodSpec.methodBuilder("register").addParameter(ProcessEngineRule.class, PROCESS_ENGINE_RULE)
        .addStatement("$T configuration = $L.getProcessEngineConfiguration()", ProcessEngineConfigurationImpl.class, PROCESS_ENGINE_RULE)
        .addStatement("$T l = configuration.getCustomPostBPMNParseListeners().get(0)", BpmnParseListener.class)
        .addStatement("(($T) l).rule = this", callActivityParseListener)
        .build();
    
    MethodSpec finished = MethodSpec.methodBuilder("finished")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .addParameter(Description.class, "description")
        .addStatement("callbackI.clear()")
        .addStatement("callbackO.clear()")
        .build();
    
    MethodSpec getBinding = MethodSpec.methodBuilder("getBinding")
        .addModifiers(Modifier.PUBLIC)
        .returns(CallableElementBinding.class)
        .addStatement("return binding != null ?  binding : $T.LATEST", CallableElementBinding.class)
        .build();

    MethodSpec getBusinessKey = MethodSpec.methodBuilder("getBusinessKey")
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return businessKey")
        .build();

    MethodSpec getDefinitionKey = MethodSpec.methodBuilder("getDefinitionKey")
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return definitionKey")
        .build();

    MethodSpec getTenantId = MethodSpec.methodBuilder("getTenantId")
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return tenantId")
        .build();

    MethodSpec getVersion = MethodSpec.methodBuilder("getVersion")
        .addModifiers(Modifier.PUBLIC)
        .returns(Integer.class)
        .addStatement("return version")
        .build();

    MethodSpec getVersionTag = MethodSpec.methodBuilder("getVersionTag")
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addStatement("return versionTag")
        .build();

    return TypeSpec.classBuilder(TYPE_CALL_ACTIVITY_RULE)
        .addModifiers(Modifier.PUBLIC)
        .superclass(TestWatcher.class)
        .addField(CallableElementBinding.class, "binding")
        .addField(String.class, "businessKey")
        .addField(String.class, "definitionKey")
        .addField(String.class, "tenantId")
        .addField(Integer.class, "version")
        .addField(String.class, "versionTag")
        .addField(callbackI, "callbackI")
        .addField(callbackO, "callbackO")
        .addMethod(constructor)
        .addMethod(register)
        .addMethod(finished)
        .addMethod(getBinding)
        .addMethod(getBusinessKey)
        .addMethod(getDefinitionKey)
        .addMethod(getTenantId)
        .addMethod(getVersion)
        .addMethod(getVersionTag)
        .build();
  }
}
