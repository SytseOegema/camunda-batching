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

package io.camunda.zeebe.model.bpmn.instance;

import io.camunda.zeebe.model.bpmn.ProcessType;
import io.camunda.zeebe.model.bpmn.impl.instance.Supports;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Sebastian Menski
 */
public class ProcessTest extends BpmnModelElementInstanceTest {

  @Override
  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(CallableElement.class, false);
  }

  @Override
  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Arrays.asList(
        new ChildElementAssumption(Auditing.class, 0, 1),
        new ChildElementAssumption(Monitoring.class, 0, 1),
        new ChildElementAssumption(Property.class),
        new ChildElementAssumption(LaneSet.class),
        new ChildElementAssumption(FlowElement.class),
        new ChildElementAssumption(Artifact.class),
        new ChildElementAssumption(ResourceRole.class),
        new ChildElementAssumption(CorrelationSubscription.class),
        new ChildElementAssumption(Supports.class));
  }

  @Override
  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
        new AttributeAssumption("processType", false, false, ProcessType.None),
        new AttributeAssumption("isClosed", false, false, false),
        new AttributeAssumption("isExecutable"));
  }
}
