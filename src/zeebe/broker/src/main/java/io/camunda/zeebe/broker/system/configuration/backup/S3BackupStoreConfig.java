/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.broker.system.configuration.backup;

import io.camunda.zeebe.broker.system.configuration.ConfigurationEntry;
import java.util.Objects;

public class S3BackupStoreConfig implements ConfigurationEntry {

  private String bucketName;
  private String endpoint;
  private String region;
  private String accessKey;
  private String secretKey;

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(final String bucketName) {
    this.bucketName = bucketName;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(final String endpoint) {
    this.endpoint = endpoint;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(final String region) {
    this.region = region;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(final String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(final String secretKey) {
    this.secretKey = secretKey;
  }

  @Override
  public int hashCode() {
    int result = bucketName != null ? bucketName.hashCode() : 0;
    result = 31 * result + (endpoint != null ? endpoint.hashCode() : 0);
    result = 31 * result + (region != null ? region.hashCode() : 0);
    result = 31 * result + (accessKey != null ? accessKey.hashCode() : 0);
    result = 31 * result + (secretKey != null ? secretKey.hashCode() : 0);
    return result;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final S3BackupStoreConfig that = (S3BackupStoreConfig) o;

    if (!Objects.equals(bucketName, that.bucketName)) {
      return false;
    }
    if (!Objects.equals(endpoint, that.endpoint)) {
      return false;
    }
    if (!Objects.equals(region, that.region)) {
      return false;
    }
    if (!Objects.equals(accessKey, that.accessKey)) {
      return false;
    }
    return Objects.equals(secretKey, that.secretKey);
  }

  @Override
  public String toString() {
    return "S3BackupStoreConfig{"
        + "bucketName='"
        + bucketName
        + '\''
        + ", endpoint='"
        + endpoint
        + '\''
        + ", region='"
        + region
        + '\''
        + ", accessKey='"
        + accessKey
        + '\''
        + ", secretKey='"
        + "<redacted>"
        + '\''
        + '}';
  }
}
