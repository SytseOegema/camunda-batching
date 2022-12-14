/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package io.camunda.zeebe.model.bpmn;

import io.camunda.zeebe.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.xml.ModelInstance;

/**
 * A BPMN 2.0 Model
 *
 * @author Daniel Meyer
 */
public interface BpmnModelInstance extends ModelInstance {

  /**
   * @return the {@link Definitions}, root element of the Bpmn Model.
   */
  Definitions getDefinitions();

  /**
   * Set the Bpmn Definitions Root element
   *
   * @param definitions the {@link Definitions} element to set
   */
  void setDefinitions(Definitions definitions);

  /**
   * Copies the BPMN model instance but not the model. So only the wrapped DOM document is cloned.
   * Changes of the model are persistent between multiple model instances.
   *
   * @return the new BPMN model instance
   */
  @Override
  BpmnModelInstance clone();
}
