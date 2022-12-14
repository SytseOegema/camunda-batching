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

package io.camunda.zeebe.model.bpmn.test.assertions;

import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

/**
 * @author Sebastian Menski
 */
public class AttributeReferenceAssert
    extends AbstractReferenceAssert<AttributeReferenceAssert, AttributeReference<?>> {

  protected AttributeReferenceAssert(final AttributeReference<?> actual) {
    super(actual, AttributeReferenceAssert.class);
  }

  public AttributeReferenceAssert hasSourceAttribute(final Attribute<?> sourceAttribute) {
    isNotNull();

    final Attribute<String> actualSourceAttribute = actual.getReferenceSourceAttribute();

    if (!sourceAttribute.equals(actualSourceAttribute)) {
      failWithMessage(
          "Expected reference <%s> to have source attribute <%s> but was <%s>",
          actual, sourceAttribute, actualSourceAttribute);
    }

    return this;
  }
}
