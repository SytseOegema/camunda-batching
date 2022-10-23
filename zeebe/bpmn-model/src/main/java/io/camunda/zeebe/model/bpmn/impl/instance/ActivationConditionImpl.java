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

package io.camunda.zeebe.model.bpmn.impl.instance;

import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_ACTIVATION_CONDITION;

import io.camunda.zeebe.model.bpmn.instance.ActivationCondition;
import io.camunda.zeebe.model.bpmn.instance.Expression;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN element activationCondition of the BPMN tComplexGateway type
 *
 * @author Sebastian Menski
 */
public class ActivationConditionImpl extends ExpressionImpl implements ActivationCondition {

  public ActivationConditionImpl(final ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public static void registerType(final ModelBuilder modelBuilder) {
    final ModelElementTypeBuilder typeBuilder =
        modelBuilder
            .defineType(ActivationCondition.class, BPMN_ELEMENT_ACTIVATION_CONDITION)
            .namespaceUri(BPMN20_NS)
            .extendsType(Expression.class)
            .instanceProvider(
                new ModelTypeInstanceProvider<ActivationCondition>() {
                  @Override
                  public ActivationCondition newInstance(
                      final ModelTypeInstanceContext instanceContext) {
                    return new ActivationConditionImpl(instanceContext);
                  }
                });

    typeBuilder.build();
  }
}
