package org.camunda.bpm.extension.bpmndt.impl.generation;

import static org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants.TYPE_CALL_ACTIVITY_PARSE_LISTENER;

import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.camunda.bpm.engine.delegate.DelegateVariableMapping;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.camunda.bpm.extension.bpmndt.impl.GeneratorConstants;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class CallActivityParseListener implements Function<GeneratorContext, TypeSpec> {

  @Override
  public TypeSpec apply(GeneratorContext context) {
    MethodSpec parseCallActivity = MethodSpec.methodBuilder("parseCallActivity")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(Element.class, "element")
        .addParameter(ScopeImpl.class, "scope")
        .addParameter(ActivityImpl.class, "activity")
        .addStatement("$T behavior = ($T) activity.getActivityBehavior()", CallActivityBehavior.class, CallActivityBehavior.class)
        .addCode("\n")
        .addStatement("activity.setActivityBehavior(new CustomBehavior(this, behavior))")
        .build();

    return TypeSpec.classBuilder(TYPE_CALL_ACTIVITY_PARSE_LISTENER)
        .superclass(AbstractBpmnParseListener.class)
        .addField(context.getTypeName(GeneratorConstants.TYPE_CALL_ACTIVITY_RULE), "rule")
        .addMethod(parseCallActivity)
        .addType(buildCustomBehavior(context))
        .build();
  }

  protected TypeSpec buildCustomBehavior(GeneratorContext context) {
    MethodSpec constructor = MethodSpec.constructorBuilder()
        .addParameter(context.getTypeName(TYPE_CALL_ACTIVITY_PARSE_LISTENER), "l")
        .addParameter(CallActivityBehavior.class, "b")
        .addStatement("this.l = l")
        .addStatement("this.b = b")
        .build();
    
    MethodSpec execute = MethodSpec.methodBuilder("execute")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ActivityExecution.class, "execution")
        .addException(Exception.class)
        .addStatement("$T activityId = execution.getCurrentActivityId()", String.class)
        .addCode("\n")
        .addStatement("l.rule.binding = b.getCallableElement().getBinding()")
        .addStatement("l.rule.businessKey = b.getCallableElement().getBusinessKey(execution)")
        .addStatement("l.rule.definitionKey = b.getCallableElement().getDefinitionKey(execution)")
        .addStatement("l.rule.tenantId = b.getCallableElement().getDefinitionTenantId(execution)")
        .addStatement("l.rule.version = b.getCallableElement().getVersion(execution)")
        .addStatement("l.rule.versionTag = b.getCallableElement().getVersionTag(execution)")
        .addCode("\n")
        .addStatement("$T variableMapping = ($T) b.resolveDelegateClass(execution)", DelegateVariableMapping.class, DelegateVariableMapping.class)
        .addCode("\n")
        .addStatement("$T subVariables = $T.createVariables()", VariableMap.class, Variables.class)
        .addCode("\n")
        .beginControlFlow("if (variableMapping != null)")
        .addCode("// map input\n")
        .addStatement("variableMapping.mapInputVariables(execution, subVariables)")
        .endControlFlow()
        .addCode("\n// create sub execution\n")
        .addStatement("$T subInstance = execution.createExecution()", ActivityExecution.class)
        .addStatement("subInstance.setVariables(subVariables)")
        .addCode("\n// execute input callback\n")
        .addStatement("l.rule.callbackI.get(activityId).accept(subInstance)")
        .addCode("\n")
        .beginControlFlow("if (variableMapping != null)")
        .addCode("// map output\n")
        .addStatement("variableMapping.mapOutputVariables(execution, subInstance)")
        .endControlFlow()
        .addCode("\n// remove sub execution\n")
        .addStatement("subInstance.remove()")
        .addCode("\n// execute output callback\n")
        .addStatement("l.rule.callbackO.get(activityId).accept(execution)")
        .addCode("\n")
        .addStatement("super.execute(execution)")
        .build();

    return TypeSpec.classBuilder("CustomBehavior")
        .addModifiers(Modifier.PRIVATE)
        .superclass(TaskActivityBehavior.class)
        .addField(context.getTypeName(TYPE_CALL_ACTIVITY_PARSE_LISTENER), "l", Modifier.PRIVATE, Modifier.FINAL)
        .addField(CallActivityBehavior.class, "b", Modifier.PRIVATE, Modifier.FINAL)
        .addMethod(constructor)
        .addMethod(execute)
        .build();
  }
}
