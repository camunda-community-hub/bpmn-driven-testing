// needed to fulfill excluded org.camunda.bpm.dmn:camunda-engine-feel-scala dependency
/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.dmn.feel.impl.scala.function;

import java.util.List;
import java.util.function.Function;

import org.camunda.bpm.dmn.feel.impl.scala.function.builder.CustomFunctionBuilder;

public class CustomFunction {

  public static CustomFunctionBuilder create() {
    return null;
  }

  public List<String> getParams() {
    return null;
  }

  public void setParams(List<String> params) {
  }

  public Function<List<Object>, Object> getFunction() {
    return null;
  }

  public void setFunction(Function<List<Object>, Object> function) {
  }

  public boolean hasVarargs() {
    return false;
  }

  public void setHasVarargs(boolean hasVarargs) {
  }
}
