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

package io.camunda.zeebe.model.bpmn.builder;

/**
 * @author Sebastian Menski
 */
@SuppressWarnings("rawtypes")
public class AbstractEmbeddedSubProcessBuilder<
    B extends AbstractEmbeddedSubProcessBuilder<B, E>, E extends AbstractSubProcessBuilder> {

  protected final E subProcessBuilder;
  protected final B myself;

  @SuppressWarnings("unchecked")
  protected AbstractEmbeddedSubProcessBuilder(final E subProcessBuilder, final Class<?> selfType) {
    myself = (B) selfType.cast(this);
    this.subProcessBuilder = subProcessBuilder;
  }
}
