package org.camunda.community.bpmndt.test;

import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

public class FieldSpecSubject extends Subject {

  public static FieldSpecSubject assertThat(FieldSpec actual) {
    return assertAbout(FieldSpecSubject::new).that(actual);
  }

  private final FieldSpec actual;

  protected FieldSpecSubject(FailureMetadata metadata, FieldSpec actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  public void hasName(String name) {
    check("name").that(actual.name).isEqualTo(name);
  }

  public void hasType(TypeName type) {
    check("type").that(actual.type).isEqualTo(type);
  }
}
