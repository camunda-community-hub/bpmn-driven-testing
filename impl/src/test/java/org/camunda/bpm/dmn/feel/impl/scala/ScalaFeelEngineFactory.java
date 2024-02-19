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
package org.camunda.bpm.dmn.feel.impl.scala;

import java.util.List;

import org.camunda.bpm.dmn.feel.impl.FeelEngine;
import org.camunda.bpm.dmn.feel.impl.FeelEngineFactory;
import org.camunda.bpm.dmn.feel.impl.scala.function.FeelCustomFunctionProvider;

public class ScalaFeelEngineFactory implements FeelEngineFactory {

  public ScalaFeelEngineFactory() {
  }

  public ScalaFeelEngineFactory(List<FeelCustomFunctionProvider> customFunctionProviders) {
  }

  public FeelEngine createInstance() {
    return null;
  }

  public void setCustomFunctionProviders(List<FeelCustomFunctionProvider> customFunctionProviders) {
  }

  public List<FeelCustomFunctionProvider> getCustomFunctionProviders() {
    return null;
  }
}
