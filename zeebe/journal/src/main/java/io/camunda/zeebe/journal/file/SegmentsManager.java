/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.journal.file;

import static com.google.common.base.Preconditions.checkNotNull;

import io.camunda.zeebe.journal.CorruptedJournalException;
import io.camunda.zeebe.journal.JournalException;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Create new segments. Load existing segments from the disk. Keep track of all segments. */
final class SegmentsManager {

  private static final long FIRST_SEGMENT_ID = 1;
  private static final long INITIAL_INDEX = 1;
  private static final long INITIAL_ASQN = SegmentedJournal.ASQN_IGNORE;

  private static final Logger LOG = LoggerFactory.getLogger(SegmentsManager.class);

  private final JournalMetrics journalMetrics;
  private final NavigableMap<Long, Segment> segments = new ConcurrentSkipListMap<>();
  private volatile Segment currentSegment;

  private final JournalIndex journalIndex;
  private final int maxSegmentSize;

  private final File directory;

  private final SegmentLoader segmentLoader;

  private final long lastWrittenIndex;

  private final String name;

  SegmentsManager(
      final JournalIndex journalIndex,
      final int maxSegmentSize,
      final File directory,
      final long lastWrittenIndex,
      final String name,
      final SegmentLoader segmentLoader) {
    this.name = checkNotNull(name, "name cannot be null");
    journalMetrics = new JournalMetrics(name);
    this.journalIndex = journalIndex;
    this.maxSegmentSize = maxSegmentSize;
    this.directory = directory;
    this.lastWrittenIndex = lastWrittenIndex;
    this.segmentLoader = segmentLoader;
  }

  void close() {
    segments
        .values()
        .forEach(
            segment -> {
              LOG.debug("Closing segment: {}", segment);
              segment.close();
            });
    currentSegment = null;
  }

  Segment getCurrentSegment() {
    return currentSegment;
  }

  Segment getFirstSegment() {
    final Map.Entry<Long, Segment> segment = segments.firstEntry();
    return segment != null ? segment.getValue() : null;
  }

  Segment getLastSegment() {
    final Map.Entry<Long, Segment> segment = segments.lastEntry();
    return segment != null ? segment.getValue() : null;
  }

  /**
   * Creates and returns the next segment.
   *
   * @return The next segment.
   * @throws IllegalStateException if the segment manager is not open
   */
  Segment getNextSegment() {

    final Segment lastSegment = getLastSegment();
    final SegmentDescriptor descriptor =
        SegmentDescriptor.builder()
            .withId(lastSegment != null ? lastSegment.descriptor().id() + 1 : 1)
            .withIndex(currentSegment.lastIndex() + 1)
            .withMaxSegmentSize(maxSegmentSize)
            .build();

    currentSegment =
        createSegment(descriptor, lastSegment != null ? lastSegment.lastAsqn() : INITIAL_ASQN);

    segments.put(descriptor.index(), currentSegment);
    journalMetrics.incSegmentCount();
    return currentSegment;
  }

  Segment getNextSegment(final long index) {
    final Map.Entry<Long, Segment> nextSegment = segments.higherEntry(index);
    return nextSegment != null ? nextSegment.getValue() : null;
  }

  Segment getSegment(final long index) {
    // Check if the current segment contains the given index first in order to prevent an
    // unnecessary map lookup.
    if (currentSegment != null && index > currentSegment.index()) {
      return currentSegment;
    }

    // If the index is in another segment, get the entry with the next lowest first index.
    final Map.Entry<Long, Segment> segment = segments.floorEntry(index);
    if (segment != null) {
      return segment.getValue();
    }
    return getFirstSegment();
  }

  private long getFirstIndex() {
    final var firstSegment = getFirstSegment();
    return firstSegment != null ? firstSegment.index() : 0;
  }

  void deleteUntil(final long index) {
    final Map.Entry<Long, Segment> segmentEntry = segments.floorEntry(index);
    if (segmentEntry != null) {
      final SortedMap<Long, Segment> compactSegments =
          segments.headMap(segmentEntry.getValue().index());
      if (compactSegments.isEmpty()) {
        LOG.debug(
            "No segments can be deleted with index < {} (first log index: {})",
            index,
            getFirstIndex());
        return;
      }

      LOG.debug(
          "{} - Deleting log up from {} up to {} (removing {} segments)",
          name,
          getFirstIndex(),
          compactSegments.get(compactSegments.lastKey()).index(),
          compactSegments.size());
      for (final Segment segment : compactSegments.values()) {
        LOG.trace("{} - Deleting segment: {}", name, segment);
        segment.delete();
        journalMetrics.decSegmentCount();
      }

      // removes them from the segment map
      compactSegments.clear();

      journalIndex.deleteUntil(index);
    }
  }

  /**
   * Resets and returns the first segment in the journal.
   *
   * @param index the starting index of the journal
   * @return the first segment
   */
  Segment resetSegments(final long index) {
    for (final Segment segment : segments.values()) {
      segment.delete();
      journalMetrics.decSegmentCount();
    }
    segments.clear();

    final SegmentDescriptor descriptor =
        SegmentDescriptor.builder()
            .withId(1)
            .withIndex(index)
            .withMaxSegmentSize(maxSegmentSize)
            .build();
    currentSegment = createSegment(descriptor, INITIAL_ASQN);
    segments.put(index, currentSegment);
    journalMetrics.incSegmentCount();
    return currentSegment;
  }

  /**
   * Removes a segment.
   *
   * @param segment The segment to remove.
   */
  void removeSegment(final Segment segment) {
    segments.remove(segment.index());
    journalMetrics.decSegmentCount();
    segment.delete();
    resetCurrentSegment();
  }

  /** Resets the current segment, creating a new segment if necessary. */
  private void resetCurrentSegment() {
    final Segment lastSegment = getLastSegment();
    if (lastSegment != null) {
      currentSegment = lastSegment;
    } else {
      final SegmentDescriptor descriptor =
          SegmentDescriptor.builder()
              .withId(FIRST_SEGMENT_ID)
              .withIndex(INITIAL_INDEX)
              .withMaxSegmentSize(maxSegmentSize)
              .build();

      currentSegment = createSegment(descriptor, INITIAL_ASQN);

      segments.put(1L, currentSegment);
      journalMetrics.incSegmentCount();
    }
  }

  /** Loads existing segments from the disk * */
  void open() {
    final var openDurationTimer = journalMetrics.startJournalOpenDurationTimer();
    // Load existing log segments from disk.
    for (final Segment segment : loadSegments()) {
      segments.put(segment.descriptor().index(), segment);
      journalMetrics.incSegmentCount();
    }

    // If a segment doesn't already exist, create an initial segment starting at index 1.
    if (!segments.isEmpty()) {
      currentSegment = segments.lastEntry().getValue();
    } else {
      final SegmentDescriptor descriptor =
          SegmentDescriptor.builder()
              .withId(FIRST_SEGMENT_ID)
              .withIndex(INITIAL_INDEX)
              .withMaxSegmentSize(maxSegmentSize)
              .build();

      currentSegment = createSegment(descriptor, INITIAL_ASQN);

      segments.put(1L, currentSegment);
      journalMetrics.incSegmentCount();
    }
    // observe the journal open duration
    openDurationTimer.close();

    // Delete files that were previously marked for deletion but did not get deleted because the
    // node was stopped. It is safe to delete it now since there are no readers opened for these
    // segments.
    deleteDeferredFiles();
  }

  private Segment createSegment(final SegmentDescriptor descriptor, final long lastWrittenAsqn) {
    final var segmentFile = SegmentFile.createSegmentFile(name, directory, descriptor.id());
    return segmentLoader.createSegment(
        segmentFile.toPath(), descriptor, lastWrittenIndex, lastWrittenAsqn, journalIndex);
  }

  /**
   * Loads all segments from disk.
   *
   * @return A collection of segments for the log.
   */
  private Collection<Segment> loadSegments() {
    // Ensure log directories are created.
    directory.mkdirs();
    final List<Segment> segments = new ArrayList<>();

    final List<File> files = getSortedLogSegments();
    Segment previousSegment = null;
    for (int i = 0; i < files.size(); i++) {
      final File file = files.get(i);

      try {
        LOG.debug("Found segment file: {}", file.getName());
        final Segment segment =
            segmentLoader.loadExistingSegment(
                file.toPath(),
                lastWrittenIndex,
                previousSegment != null ? previousSegment.lastAsqn() : INITIAL_ASQN,
                journalIndex);

        if (i > 0) {
          checkForIndexGaps(segments.get(i - 1), segment);
        }

        segments.add(segment);
        previousSegment = segment;
      } catch (final CorruptedJournalException e) {
        if (handleSegmentCorruption(files, segments, i)) {
          return segments;
        }

        throw e;
      }
    }

    return segments;
  }

  private void checkForIndexGaps(final Segment prevSegment, final Segment segment) {
    if (prevSegment.lastIndex() != segment.index() - 1) {
      throw new CorruptedJournalException(
          String.format(
              "Log segment %s is not aligned with previous segment %s (last index: %d).",
              segment, prevSegment, prevSegment.lastIndex()));
    }
  }

  /** Returns true if segments after corrupted segment were deleted; false, otherwise */
  private boolean handleSegmentCorruption(
      final List<File> files, final List<Segment> segments, final int failedIndex) {
    long lastSegmentIndex = 0;

    if (!segments.isEmpty()) {
      final Segment previousSegment = segments.get(segments.size() - 1);
      lastSegmentIndex = previousSegment.lastIndex();
    }

    if (lastWrittenIndex > lastSegmentIndex) {
      return false;
    }

    LOG.debug(
        "Found corrupted segment after last ack'ed index {}. Deleting segments {} - {}",
        lastWrittenIndex,
        files.get(failedIndex).getName(),
        files.get(files.size() - 1).getName());

    for (int i = failedIndex; i < files.size(); i++) {
      final File file = files.get(i);
      try {
        Files.delete(file.toPath());
      } catch (final IOException e) {
        throw new JournalException(
            String.format(
                "Failed to delete log segment '%s' when handling corruption.", file.getName()),
            e);
      }
    }

    return true;
  }

  /** Returns an array of valid log segments sorted by their id which may be empty but not null. */
  private List<File> getSortedLogSegments() {
    final File[] files =
        directory.listFiles(file -> file.isFile() && SegmentFile.isSegmentFile(name, file));

    if (files == null) {
      throw new IllegalStateException(
          String.format(
              "Could not list files in directory '%s'. Either the path doesn't point to a directory or an I/O error occurred.",
              directory));
    }

    Arrays.sort(files, Comparator.comparingInt(f -> SegmentFile.getSegmentIdFromPath(f.getName())));

    return Arrays.asList(files);
  }

  private void deleteDeferredFiles() {
    try (final DirectoryStream<Path> segmentsToDelete =
        Files.newDirectoryStream(
            directory.toPath(),
            path -> SegmentFile.isDeletedSegmentFile(name, path.getFileName().toString()))) {
      segmentsToDelete.forEach(this::deleteDeferredFile);
    } catch (final IOException e) {
      LOG.warn(
          "Could not delete segment files marked for deletion in {}. This can result in unnecessary disk usage.",
          directory.toPath(),
          e);
    }
  }

  private void deleteDeferredFile(final Path segmentFileToDelete) {
    try {
      Files.deleteIfExists(segmentFileToDelete);
    } catch (final IOException e) {
      LOG.warn(
          "Could not delete file {} which is marked for deletion. This can result in unnecessary disk usage.",
          segmentFileToDelete,
          e);
    }
  }
}
