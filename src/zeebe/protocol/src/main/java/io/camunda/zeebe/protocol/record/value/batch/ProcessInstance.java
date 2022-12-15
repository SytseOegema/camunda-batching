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
package io.camunda.zeebe.protocol.record.value.batch;

import io.camunda.zeebe.protocol.record.ImmutableProtocol;
import io.camunda.zeebe.protocol.record.RecordValueWithVariables;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import org.agrona.DirectBuffer;
import org.immutables.value.Value;

/** Represents a single deployment resource. */
@Value.Immutable
@ImmutableProtocol(builder = ImmutableProcessInstance.Builder.class)
public interface ProcessInstance extends RecordValueWithVariables {

  public long getProcessInstanceKey();

  public long getElementInstanceKey();

  public String getBpmnProcessId();

  public DirectBuffer getBpmnProcessIdBuffer();

  public int getProcessVersion();

  public long getProcessDefinitionKey();

  public String getElementId();

  public BpmnElementType getBpmnElementType();

  public String getBpmnElementTypeDescription();

  public long getFlowScopeKey();

  public DirectBuffer getVariablesBuffer();
}
