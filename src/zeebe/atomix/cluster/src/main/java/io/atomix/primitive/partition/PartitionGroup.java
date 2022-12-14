/*
 * Copyright 2017-present Open Networking Foundation
 * Copyright © 2020 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.primitive.partition;

import com.google.common.hash.Hashing;
import io.atomix.cluster.MemberId;
import io.atomix.utils.NamedType;
import io.atomix.utils.config.Configured;
import io.atomix.utils.serializer.Namespace;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/** Primitive partition group. */
public interface PartitionGroup extends Configured<PartitionGroupConfig> {

  /**
   * Returns the partition group name.
   *
   * @return the partition group name
   */
  String name();

  /**
   * Returns a partition by ID. Assumes that the partition ID belongs to this group.
   *
   * @param partitionId the partition identifier
   * @return the partition or {@code null} if no partition with the given identifier exists
   */
  Partition getPartition(int partitionId);

  /**
   * Returns a partition by ID.
   *
   * @param partitionId the partition identifier
   * @return the partition or {@code null} if no partition with the given identifier exists
   * @throws NullPointerException if the partition identifier is {@code null}
   */
  Partition getPartition(PartitionId partitionId);

  /**
   * Returns the partition for the given key.
   *
   * @param key the key for which to return the partition
   * @return the partition for the given key
   */
  default Partition getPartition(final String key) {
    final int hashCode = Hashing.sha256().hashString(key, StandardCharsets.UTF_8).asInt();
    return getPartition(getPartitionIds().get(Math.abs(hashCode) % getPartitionIds().size()));
  }

  /**
   * Returns a collection of all partitions.
   *
   * @return a collection of all partitions
   */
  Collection<Partition> getPartitions();

  /**
   * Returns a sorted list of partition IDs.
   *
   * @return a sorted list of partition IDs
   */
  List<PartitionId> getPartitionIds();

  default List<Partition> getPartitionsWithMember(final MemberId memberId) {
    return getPartitions().stream()
        .filter(partition -> partition.members().contains(memberId))
        .collect(Collectors.toList());
  }

  /** Partition group builder. */
  abstract class Builder<C extends PartitionGroupConfig<C>>
      implements io.atomix.utils.Builder<ManagedPartitionGroup> {
    protected final C config;

    protected Builder(final C config) {
      this.config = config;
    }
  }

  /** Partition group type. */
  interface Type<C extends PartitionGroupConfig<C>> extends NamedType {

    /**
     * Returns the partition group namespace.
     *
     * @return the partition group namespace
     */
    Namespace namespace();

    /**
     * Creates a new partition group instance.
     *
     * @param config the partition group configuration
     * @return the partition group
     */
    ManagedPartitionGroup newPartitionGroup(C config);
  }
}
