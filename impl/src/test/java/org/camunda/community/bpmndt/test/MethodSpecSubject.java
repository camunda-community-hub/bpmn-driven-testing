package org.camunda.community.bpmndt.test;

import static com.google.common.truth.Truth.assertAbout;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Modifier;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

public class MethodSpecSubject extends Subject {

  public static MethodSpecSubject assertThat(MethodSpec actual) {
    return assertAbout(MethodSpecSubject::new).that(actual);
  }

  private final MethodSpec actual;

  protected MethodSpecSubject(FailureMetadata metadata, MethodSpec actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  public void containsCode(String substring) {
    check("code").that(actual.code.toString()).contains(substring);
  }

  public void hasAnnotation(Class<? extends Annotation> annotationType) {
    check("annotations").that(actual.annotations).hasSize(1);
    check("annotations[0].type").that(actual.annotations.get(0).type).isEqualTo(TypeName.get(annotationType));
  }

  public void hasJavaDoc() {
    check("javadoc").that(actual.javadoc.toString()).isNotEmpty();
  }

  public void hasName(String name) {
    check("name").that(actual.name).isEqualTo(name);
  }

  public void hasParameters(String... parameterNames) {
    check("parameters").that(actual.parameters).hasSize(parameterNames.length);

    String[] actualParameterNames = actual.parameters.stream().map(parameter -> parameter.name).toArray(String[]::new);
    check("parameters").that(actualParameterNames).isEqualTo(parameterNames);
  }

  public void hasReturnType(TypeName returnType) {
    check("returnType").that(actual.returnType).isEqualTo(returnType);
  }

  public void isProtected() {
    check("modifiers").that(actual.modifiers).containsExactly(Modifier.PROTECTED);
  }

  public void isPublic() {
    check("modifiers").that(actual.modifiers).containsExactly(Modifier.PUBLIC);
  }

  public void notContainsCode(String substring) {
    check("code").that(actual.code.toString()).doesNotContain(substring);
  }
}
