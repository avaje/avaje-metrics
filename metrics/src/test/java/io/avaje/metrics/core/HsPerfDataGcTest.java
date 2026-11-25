package io.avaje.metrics.core;

import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.Metric;
import io.avaje.metrics.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HsPerfDataGcTest {

  @Test
  void g1CollectorsAndIncrementalCollection() {
    PerfBufferBuilder builder = new PerfBufferBuilder(ByteOrder.LITTLE_ENDIAN, true);
    builder.addLong("java.threads.live", 12L);
    builder.addLong("sun.os.hrt.frequency", 1_000_000_000L);

    builder.addString("sun.gc.collector.0.name", "G1 young collection pauses");
    int youngCountOffset = builder.addLong("sun.gc.collector.0.invocations", 3L);
    int youngTimeOffset = builder.addLong("sun.gc.collector.0.time", 12_600_000L); // 12.6 ms -> 12 ms

    builder.addString("sun.gc.collector.1.name", "G1 full collection pauses");
    int fullCountOffset = builder.addLong("sun.gc.collector.1.invocations", 0L);
    int fullTimeOffset = builder.addLong("sun.gc.collector.1.time", 0L);

    builder.addString("sun.gc.collector.2.name", "G1 concurrent cycle pauses");
    int concCountOffset = builder.addLong("sun.gc.collector.2.invocations", 1L);
    int concTimeOffset = builder.addLong("sun.gc.collector.2.time", 2_500_000L); // 2.5 ms -> 2 ms

    ByteBuffer buffer = builder.build();
    List<HsPerfDataGc.Collector> collectors = HsPerfDataGc.parse(buffer);

    assertThat(collectors).hasSize(3);
    assertThat(collectors.get(0).name()).isEqualTo("G1 young collection pauses");
    assertThat(collectors.get(1).name()).isEqualTo("G1 full collection pauses");
    assertThat(collectors.get(2).name()).isEqualTo("G1 concurrent cycle pauses");

    DefaultMetricProvider registry = new DefaultMetricProvider();
    Tags tags = Tags.of("env:test");
    JvmGarbageCollection.registerPerfCollectors(registry, collectors, tags);

    Map<String, Long> first = toSeriesMap(registry.collectMetrics());
    assertThat(first)
      .hasSize(6)
      .containsEntry("jvm.gc.count[env:test, gc:young]", 3L)
      .containsEntry("jvm.gc.time[env:test, gc:young]", 12L)
      .containsEntry("jvm.gc.count[env:test, gc:full]", 0L)
      .containsEntry("jvm.gc.time[env:test, gc:full]", 0L)
      .containsEntry("jvm.gc.count[env:test, gc:concurrent]", 1L)
      .containsEntry("jvm.gc.time[env:test, gc:concurrent]", 2L);

    // Simulate C++ G1 PerfDataThread publishing updated counters into shared memory
    buffer.putLong(youngCountOffset, 8L);
    buffer.putLong(youngTimeOffset, 32_600_000L); // +20 ms (32.6 ms cumulative)
    buffer.putLong(fullCountOffset, 1L);
    buffer.putLong(fullTimeOffset, 45_000_000L); // +45 ms
    buffer.putLong(concCountOffset, 2L);
    buffer.putLong(concTimeOffset, 5_500_000L); // +3 ms (5.5 ms cumulative)

    Map<String, Long> second = toSeriesMap(registry.collectMetrics());
    assertThat(second)
      .hasSize(6)
      .containsEntry("jvm.gc.count[env:test, gc:young]", 5L)
      .containsEntry("jvm.gc.time[env:test, gc:young]", 20L)
      .containsEntry("jvm.gc.count[env:test, gc:full]", 1L)
      .containsEntry("jvm.gc.time[env:test, gc:full]", 45L)
      .containsEntry("jvm.gc.count[env:test, gc:concurrent]", 1L)
      .containsEntry("jvm.gc.time[env:test, gc:concurrent]", 3L);
  }

  @Test
  void gcTagNormalizationAcrossHotSpotAndGraalVm() {
    // HotSpot G1 and GraalVM Native Image G1 map to identical young / full / concurrent tags
    assertThat(JvmGarbageCollection.gcTag("G1 Young Generation")).isEqualTo("young");
    assertThat(JvmGarbageCollection.gcTag("G1 young collection pauses")).isEqualTo("young");
    assertThat(JvmGarbageCollection.gcTag("G1 Old Generation")).isEqualTo("full");
    assertThat(JvmGarbageCollection.gcTag("G1 full collection pauses")).isEqualTo("full");
    assertThat(JvmGarbageCollection.gcTag("G1 Concurrent GC")).isEqualTo("concurrent");
    assertThat(JvmGarbageCollection.gcTag("G1 concurrent cycle pauses")).isEqualTo("concurrent");

    // Serial and Parallel collectors across HotSpot and GraalVM Native Image
    assertThat(JvmGarbageCollection.gcTag("Serial young collection pauses")).isEqualTo("young");
    assertThat(JvmGarbageCollection.gcTag("young generation scavenger")).isEqualTo("young");
    assertThat(JvmGarbageCollection.gcTag("PS Scavenge")).isEqualTo("young");
    assertThat(JvmGarbageCollection.gcTag("Copy")).isEqualTo("young");
    assertThat(JvmGarbageCollection.gcTag("Serial full collection pauses")).isEqualTo("full");
    assertThat(JvmGarbageCollection.gcTag("complete scavenger")).isEqualTo("full");
    assertThat(JvmGarbageCollection.gcTag("PS MarkSweep")).isEqualTo("full");
    assertThat(JvmGarbageCollection.gcTag("MarkSweepCompact")).isEqualTo("full");

    // Generational ZGC and other collectors preserve distinct tag values
    assertThat(JvmGarbageCollection.gcTag("ZGC Minor Pauses")).isEqualTo("zgc_minor_pauses");
    assertThat(JvmGarbageCollection.gcTag("ZGC Minor Cycles")).isEqualTo("zgc_minor_cycles");
    assertThat(JvmGarbageCollection.gcTag("ZGC Major Pauses")).isEqualTo("zgc_major_pauses");
    assertThat(JvmGarbageCollection.gcTag("ZGC Major Cycles")).isEqualTo("zgc_major_cycles");
    assertThat(JvmGarbageCollection.gcTag("Shenandoah Pauses")).isEqualTo("shenandoah_pauses");
    assertThat(JvmGarbageCollection.gcTag("Shenandoah Cycles")).isEqualTo("concurrent");

    // Collision guard falls back to normalized collector name instead of overwriting
    Set<String> used = new HashSet<>();
    assertThat(JvmGarbageCollection.collectorTags(Tags.EMPTY, "G1 Young Generation", used).array())
      .containsExactly("gc:young");
    assertThat(JvmGarbageCollection.collectorTags(Tags.EMPTY, "Copy", used).array())
      .containsExactly("gc:copy");
  }

  @Test
  void bigEndianAndNonGigahertzFrequency() {
    PerfBufferBuilder builder = new PerfBufferBuilder(ByteOrder.BIG_ENDIAN, true);
    builder.addLong("sun.os.hrt.frequency", 24_000_000L); // 24 MHz timer
    builder.addString("sun.gc.collector.0.name", "G1 young collection pauses");
    builder.addLong("sun.gc.collector.0.invocations", 4L);
    builder.addLong("sun.gc.collector.0.time", 360_000L); // 15 ms at 24 MHz

    List<HsPerfDataGc.Collector> collectors = HsPerfDataGc.parse(builder.build());
    assertThat(collectors).hasSize(1);
    assertThat(collectors.get(0).collectionCount()).isEqualTo(4L);
    assertThat(collectors.get(0).collectionTimeMillis()).isEqualTo(15L);

    // Verify non-divisible frequency and large tick count without overflow
    assertThat(HsPerfDataGc.ticksToMillis(24_691_356L, 12_345_678L)).isEqualTo(2000L);
    assertThat(HsPerfDataGc.ticksToMillis(Long.MAX_VALUE / 2, 1_000_000_000L))
      .isEqualTo((Long.MAX_VALUE / 2) / 1_000_000L);
  }

  @Test
  void rejectsInaccessibleInvalidAndIncompleteEntries() {
    PerfBufferBuilder uninitialized = new PerfBufferBuilder(ByteOrder.LITTLE_ENDIAN, false);
    uninitialized.addString("sun.gc.collector.0.name", "G1 young collection pauses");
    uninitialized.addLong("sun.gc.collector.0.invocations", 1L);
    uninitialized.addLong("sun.gc.collector.0.time", 1_000_000L);
    assertThat(HsPerfDataGc.parse(uninitialized.build())).isEmpty();

    // Short buffer and bad magic
    assertThat(HsPerfDataGc.parse(ByteBuffer.allocate(16))).isEmpty();
    assertThat(HsPerfDataGc.parse(ByteBuffer.allocate(64))).isEmpty();

    // Incomplete collector (missing .time) is ignored
    PerfBufferBuilder incomplete = new PerfBufferBuilder(ByteOrder.LITTLE_ENDIAN, true);
    incomplete.addString("sun.gc.collector.0.name", "Broken collector");
    incomplete.addLong("sun.gc.collector.0.invocations", 5L);
    assertThat(HsPerfDataGc.parse(incomplete.build())).isEmpty();
  }

  @Test
  void openFileAndCurrentJvmDiscovery(@TempDir Path tempDir) throws IOException {
    PerfBufferBuilder builder = new PerfBufferBuilder(ByteOrder.nativeOrder(), true);
    builder.addLong("sun.os.hrt.frequency", 1_000_000_000L);
    builder.addString("sun.gc.collector.0.name", "G1 young collection pauses");
    builder.addLong("sun.gc.collector.0.invocations", 7L);
    builder.addLong("sun.gc.collector.0.time", 9_000_000L);

    ByteBuffer raw = builder.build();
    byte[] bytes = new byte[raw.capacity()];
    raw.duplicate().get(bytes);

    Path file = tempDir.resolve("12345");
    Files.write(file, bytes);

    List<HsPerfDataGc.Collector> fromFile = HsPerfDataGc.openFile(file);
    assertThat(fromFile).hasSize(1);
    assertThat(fromFile.get(0).collectionCount()).isEqualTo(7L);
    assertThat(fromFile.get(0).collectionTimeMillis()).isEqualTo(9L);

    // Current JVM runs with UsePerfData enabled by default on HotSpot
    List<HsPerfDataGc.Collector> live = HsPerfDataGc.discover();
    assertThat(live).isNotEmpty();
    for (HsPerfDataGc.Collector collector : live) {
      assertThat(collector.name()).isNotEmpty();
      assertThat(collector.collectionCount()).isGreaterThanOrEqualTo(0L);
      assertThat(collector.collectionTimeMillis()).isGreaterThanOrEqualTo(0L);
    }
  }

  private static Map<String, Long> toSeriesMap(List<Metric.Statistics> stats) {
    Map<String, Long> map = new HashMap<>();
    for (Metric.Statistics stat : stats) {
      GaugeLong.Stats gauge = (GaugeLong.Stats) stat;
      map.put(stat.name() + Arrays.toString(stat.tags()), gauge.value());
    }
    return map;
  }

  private static final class PerfBufferBuilder {

    private final ByteBuffer buf;
    private int pos = HsPerfDataGc.PROLOGUE_SIZE;
    private int entries;

    PerfBufferBuilder(ByteOrder order, boolean accessible) {
      this.buf = ByteBuffer.allocate(4096);
      buf.order(ByteOrder.BIG_ENDIAN);
      buf.putInt(0, HsPerfDataGc.PERFDATA_MAGIC);
      buf.put(4, (byte) (order == ByteOrder.BIG_ENDIAN ? 0 : 1));
      buf.put(5, (byte) 2);
      buf.put(6, (byte) 0);
      buf.put(7, (byte) (accessible ? 1 : 0));
      buf.order(order);
      buf.putInt(24, HsPerfDataGc.PROLOGUE_SIZE);
    }

    int addLong(String name, long value) {
      byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
      int meta = align(HsPerfDataGc.ENTRY_HEADER_SIZE + nameBytes.length + 1, 8);
      int total = align(meta + 8, 8);

      writeEntryHeader(total, 0, (byte) 'J', meta, nameBytes);
      int valueOffset = pos + meta;
      buf.putLong(valueOffset, value);
      pos += total;
      entries++;
      return valueOffset;
    }

    void addString(String name, String value) {
      byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
      byte[] valBytes = value.getBytes(StandardCharsets.UTF_8);
      int meta = HsPerfDataGc.ENTRY_HEADER_SIZE + nameBytes.length + 1;
      int vecLen = valBytes.length + 1;
      int total = align(meta + vecLen, 8);

      writeEntryHeader(total, vecLen, (byte) 'B', meta, nameBytes);
      for (int i = 0; i < valBytes.length; i++) {
        buf.put(pos + meta + i, valBytes[i]);
      }
      buf.put(pos + meta + valBytes.length, (byte) 0);
      pos += total;
      entries++;
    }

    private void writeEntryHeader(int total, int vecLen, byte type, int dataOffset, byte[] nameBytes) {
      buf.putInt(pos, total);
      buf.putInt(pos + 4, HsPerfDataGc.ENTRY_HEADER_SIZE);
      buf.putInt(pos + 8, vecLen);
      buf.put(pos + 12, type);
      buf.put(pos + 13, (byte) 0);
      buf.put(pos + 14, (byte) 0);
      buf.put(pos + 15, (byte) 0);
      buf.putInt(pos + 16, dataOffset);
      for (int i = 0; i < nameBytes.length; i++) {
        buf.put(pos + HsPerfDataGc.ENTRY_HEADER_SIZE + i, nameBytes[i]);
      }
      buf.put(pos + HsPerfDataGc.ENTRY_HEADER_SIZE + nameBytes.length, (byte) 0);
    }

    ByteBuffer build() {
      buf.putInt(8, pos);
      buf.putInt(28, entries);
      return buf;
    }

    private static int align(int value, int alignment) {
      int rem = value % alignment;
      return rem == 0 ? value : value + (alignment - rem);
    }
  }
}
