/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.broker.system.configuration;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.camunda.zeebe.util.FeatureFlags;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonSerialize // this annotation seems to implicitly disable
// SerializationFeature.FAIL_ON_EMPTY_BEANS
public final class FeatureFlagsCfg {

  // To add a new feature flag, read the comments in FeatureFlags

  private static final FeatureFlags DEFAULT_SETTINGS = FeatureFlags.createDefault();
  //
  //  private boolean enableFoo = DEFAULT_SETTINGS.foo();
  //
  //  public boolean isEnableFoo() {
  //    return enableFoo;
  //  }
  //
  //  public void setEnableFoo(final boolean enableFoo) {
  //    this.enableFoo = enableFoo;
  //  }

  private boolean enableYieldingDueDateChecker = DEFAULT_SETTINGS.yieldingDueDateChecker();
  private boolean enableActorMetrics = DEFAULT_SETTINGS.enableActorMetrics();
  private boolean enableBackup = DEFAULT_SETTINGS.enableBackup();

  public boolean isEnableYieldingDueDateChecker() {
    return enableYieldingDueDateChecker;
  }

  public void setEnableYieldingDueDateChecker(final boolean enableYieldingDueDateChecker) {
    this.enableYieldingDueDateChecker = enableYieldingDueDateChecker;
  }

  public boolean isEnableActorMetrics() {
    return enableActorMetrics;
  }

  public void setEnableActorMetrics(final boolean enableActorMetrics) {
    this.enableActorMetrics = enableActorMetrics;
  }

  public boolean isEnableBackup() {
    return enableBackup;
  }

  public void setEnableBackup(final boolean enableBackup) {
    this.enableBackup = enableBackup;
  }

  public FeatureFlags toFeatureFlags() {
    return new FeatureFlags(
        enableYieldingDueDateChecker, enableActorMetrics, enableBackup /*, enableFoo*/);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
