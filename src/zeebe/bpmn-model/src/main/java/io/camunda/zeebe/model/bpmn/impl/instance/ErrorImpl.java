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

import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ATTRIBUTE_ERROR_CODE;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ATTRIBUTE_NAME;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ATTRIBUTE_STRUCTURE_REF;
import static io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_ERROR;

import io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants;
import io.camunda.zeebe.model.bpmn.instance.Error;
import io.camunda.zeebe.model.bpmn.instance.ItemDefinition;
import io.camunda.zeebe.model.bpmn.instance.RootElement;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.reference.AttributeReference;

/**
 * @author Sebastian Menski
 */
public class ErrorImpl extends RootElementImpl implements Error {

  protected static Attribute<String> nameAttribute;
  protected static Attribute<String> errorCodeAttribute;

  protected static AttributeReference<ItemDefinition> structureRefAttribute;

  public ErrorImpl(final ModelTypeInstanceContext context) {
    super(context);
  }

  public static void registerType(final ModelBuilder modelBuilder) {
    final ModelElementTypeBuilder typeBuilder =
        modelBuilder
            .defineType(Error.class, BPMN_ELEMENT_ERROR)
            .namespaceUri(BpmnModelConstants.BPMN20_NS)
            .extendsType(RootElement.class)
            .instanceProvider(
                new ModelTypeInstanceProvider<Error>() {
                  @Override
                  public Error newInstance(final ModelTypeInstanceContext instanceContext) {
                    return new ErrorImpl(instanceContext);
                  }
                });

    nameAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_NAME).build();

    errorCodeAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_ERROR_CODE).build();

    structureRefAttribute =
        typeBuilder
            .stringAttribute(BPMN_ATTRIBUTE_STRUCTURE_REF)
            .qNameAttributeReference(ItemDefinition.class)
            .build();

    typeBuilder.build();
  }

  @Override
  public String getName() {
    return nameAttribute.getValue(this);
  }

  @Override
  public void setName(final String name) {
    nameAttribute.setValue(this, name);
  }

  @Override
  public String getErrorCode() {
    return errorCodeAttribute.getValue(this);
  }

  @Override
  public void setErrorCode(final String errorCode) {
    errorCodeAttribute.setValue(this, errorCode);
  }

  @Override
  public ItemDefinition getStructure() {
    return structureRefAttribute.getReferenceTargetElement(this);
  }

  @Override
  public void setStructure(final ItemDefinition structure) {
    structureRefAttribute.setReferenceTargetElement(this, structure);
  }
}
