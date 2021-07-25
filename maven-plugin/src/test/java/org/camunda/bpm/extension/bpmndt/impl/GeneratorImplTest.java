package org.camunda.bpm.extension.bpmndt.impl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.camunda.bpm.extension.bpmndt.BpmnSupport;
import org.camunda.bpm.extension.bpmndt.GeneratorContext;
import org.camunda.bpm.extension.bpmndt.type.Path;
import org.camunda.bpm.extension.bpmndt.type.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.squareup.javapoet.TypeSpec;

public class GeneratorImplTest {

  private GeneratorImpl generator;

  private TestCase testCase;

  @Before
  public void setUp() {
    GeneratorContext context = Mockito.mock(GeneratorContext.class);
    when(context.getBpmnSupport()).thenReturn(Mockito.mock(BpmnSupport.class));

    generator = new GeneratorImpl(context);

    testCase = Mockito.mock(TestCase.class);
    when(testCase.getPath()).thenReturn(Mockito.mock(Path.class));
  }

  @Test
  public void testPathEmpty() {
    TypeSpec.Builder classBuilder = TypeSpec.classBuilder("Test");

    generator.buildTestCase(testCase, classBuilder);

    TypeSpec test = classBuilder.build();
    assertThat(test.methodSpecs, hasSize(7));
    assertThat(test.methodSpecs.get(1).name, equalTo(GeneratorConstants.EXECUTE));
    assertThat(test.methodSpecs.get(1).code.toString(), containsString("throw new java.lang.RuntimeException(\"Path is empty\");"));
  }

  @Test
  public void testPathNotValid() {
    when(testCase.getPath().getFlowNodeIds()).thenReturn(Collections.singletonList("not-existing"));
    
    TypeSpec.Builder classBuilder = TypeSpec.classBuilder("Test");

    generator.buildTestCase(testCase, classBuilder);

    TypeSpec test = classBuilder.build();
    assertThat(test.methodSpecs, hasSize(7));
    assertThat(test.methodSpecs.get(1).name, equalTo(GeneratorConstants.EXECUTE));
    assertThat(test.methodSpecs.get(1).code.toString(), containsString("// Not existing flow nodes:\n"));
    assertThat(test.methodSpecs.get(1).code.toString(), containsString("// not-existing\n"));
    assertThat(test.methodSpecs.get(1).code.toString(), containsString("throw new java.lang.RuntimeException(\"Path is not valid\");"));
  }
}
