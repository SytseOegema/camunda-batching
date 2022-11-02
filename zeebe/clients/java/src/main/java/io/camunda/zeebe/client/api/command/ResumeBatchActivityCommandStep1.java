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
package io.camunda.zeebe.client.api.command;

import io.camunda.zeebe.client.api.response.ResumeBatchActivityResponse;

public interface ResumeBatchActivityCommandStep1 {

  /**
   * Set the BPMN process id of the process to create an instance of. This is the static id of the
   * process in the BPMN XML (i.e. "&#60;bpmn:process id='my-process'&#62;").
   *
   * @param bpmnProcessId the BPMN process id of the process
   * @return the builder for this command
   */
  ResumeBatchActivityCommandStep2 bpmnProcessId(String bpmnProcessId);

  interface ResumeBatchActivityCommandStep2
      extends ResumeBatchActivityCommandStep1, FinalCommandStep<ResumeBatchActivityResponse> {
    // the place for new optional parameters
  }
}
