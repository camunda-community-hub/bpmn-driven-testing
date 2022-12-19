package org.camunda.community.bpmndt.test;

import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.squareup.javapoet.TypeSpec;

public class TypeSpecSubject extends Subject {

  public static TypeSpecSubject assertThat(TypeSpec actual) {
    return assertAbout(TypeSpecSubject::new).that(actual);
  }

  private final TypeSpec actual;

  protected TypeSpecSubject(FailureMetadata metadata, TypeSpec actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  public void hasJavaDoc() {
    check("javadoc").that(actual.javadoc.toString()).isNotEmpty();
  }

  public void hasFields(int expectedSize) {
    check("fieldSpecs").that(actual.fieldSpecs).hasSize(expectedSize);
  }

  public void hasMethods(int expectedSize) {
    check("methodSpecs").that(actual.methodSpecs).hasSize(expectedSize);
  }

  public void hasName(String name) {
    check("name").that(actual.name).isEqualTo(name);
  }
}
