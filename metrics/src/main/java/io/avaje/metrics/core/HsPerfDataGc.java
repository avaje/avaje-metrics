package io.avaje.metrics.core;

import io.avaje.applog.AppLog;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Reads GC collector counters from the local HotSpot / GraalVM Native Image {@code hsperfdata}
 * v2.0 shared-memory buffer when {@code GarbageCollectorMXBean} instances are unavailable
 * (such as GraalVM Native Image built with {@code --gc=G1 --enable-monitoring=jvmstat}).
 */
final class HsPerfDataGc {

  private static final System.Logger log = AppLog.getLogger("io.avaje.metrics");

  static final int PERFDATA_MAGIC = 0xcafec0c0;
  static final int PROLOGUE_SIZE = 32;
  static final int ENTRY_HEADER_SIZE = 20;

  private static final int BYTE_ORDER_OFFSET = 4;
  private static final int MAJOR_VERSION_OFFSET = 5;
  private static final int ACCESSIBLE_OFFSET = 7;
  private static final int USED_OFFSET = 8;
  private static final int ENTRY_OFFSET_OFFSET = 24;
  private static final int NUM_ENTRIES_OFFSET = 28;

  private static final byte BIG_ENDIAN_FLAG = 0;
  private static final byte LITTLE_ENDIAN_FLAG = 1;
  private static final byte EXPECTED_MAJOR_VERSION = 2;

  private static final long DEFAULT_FREQUENCY = 1_000_000_000L;
  private static final long MAX_PERFDATA_SIZE = 16L * 1024L * 1024L;
  private static final int INIT_WAIT_ATTEMPTS = 25;
  private static final long INIT_WAIT_MILLIS = 10L;

  private static final String HRT_FREQUENCY_KEY = "sun.os.hrt.frequency";
  private static final String COLLECTOR_PREFIX = "sun.gc.collector.";
  private static final String PERFDATA_DIR_PREFIX = "hsperfdata_";
  private static final String PERFDATA_THREAD_NAME = "Performance data";

  private HsPerfDataGc() {}

  /**
   * Discover GC collectors for the current process from its {@code hsperfdata} file.
   */
  static List<Collector> discover() {
    final long pid;
    try {
      pid = ProcessHandle.current().pid();
    } catch (RuntimeException e) {
      log.log(DEBUG, "Unable to determine current PID for hsperfdata lookup", e);
      return Collections.emptyList();
    }
    return discoverForPid(pid);
  }

  static List<Collector> discoverForPid(long pid) {
    Set<Path> baseDirs = candidateTempDirs();
    String user = System.getProperty("user.name");

    Path file = findPerfDataFile(baseDirs, user, pid);
    if (file != null) {
      List<Collector> collectors = openFile(file);
      if (!collectors.isEmpty()) {
        return collectors;
      }
    }

    if (!isPerfDataInitializing(file)) {
      return Collections.emptyList();
    }

    // GraalVM's "Performance data" daemon thread initializes the hsperfdata buffer asynchronously
    // at isolate startup; wait briefly if registration happens right at the start of main().
    for (int attempt = 0; attempt < INIT_WAIT_ATTEMPTS; attempt++) {
      try {
        Thread.sleep(INIT_WAIT_MILLIS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
      if (file == null) {
        file = findPerfDataFile(baseDirs, user, pid);
      }
      if (file != null) {
        List<Collector> collectors = openFile(file);
        if (!collectors.isEmpty()) {
          return collectors;
        }
      }
    }
    return Collections.emptyList();
  }

  static List<Collector> openFile(Path file) {
    try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
      long size = channel.size();
      if (size < PROLOGUE_SIZE || size > MAX_PERFDATA_SIZE) {
        return Collections.emptyList();
      }
      ByteBuffer mapped = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);
      return parse(mapped);
    } catch (IOException | RuntimeException e) {
      log.log(DEBUG, "Unable to read hsperfdata file " + file, e);
      return Collections.emptyList();
    }
  }

  /**
   * Parse GC collectors from an {@code hsperfdata} v2.0 buffer.
   * Returned collectors read live counter values directly from {@code buffer}.
   */
  static List<Collector> parse(ByteBuffer buffer) {
    if (buffer.capacity() < PROLOGUE_SIZE) {
      return Collections.emptyList();
    }
    ByteBuffer header = buffer.duplicate().order(ByteOrder.BIG_ENDIAN);
    if (header.getInt(0) != PERFDATA_MAGIC) {
      return Collections.emptyList();
    }
    byte byteOrderFlag = header.get(BYTE_ORDER_OFFSET);
    final ByteOrder order;
    if (byteOrderFlag == BIG_ENDIAN_FLAG) {
      order = ByteOrder.BIG_ENDIAN;
    } else if (byteOrderFlag == LITTLE_ENDIAN_FLAG) {
      order = ByteOrder.LITTLE_ENDIAN;
    } else {
      return Collections.emptyList();
    }
    if (header.get(MAJOR_VERSION_OFFSET) != EXPECTED_MAJOR_VERSION || header.get(ACCESSIBLE_OFFSET) == 0) {
      return Collections.emptyList();
    }

    ByteBuffer view = buffer.duplicate().order(order);
    int used = view.getInt(USED_OFFSET);
    int limit = (used >= PROLOGUE_SIZE && used <= view.capacity()) ? used : view.capacity();
    int entryOffset = view.getInt(ENTRY_OFFSET_OFFSET);
    int numEntries = view.getInt(NUM_ENTRIES_OFFSET);
    if (entryOffset < PROLOGUE_SIZE || entryOffset > limit || numEntries <= 0) {
      return Collections.emptyList();
    }

    long frequency = DEFAULT_FREQUENCY;
    Map<Integer, CollectorSlot> slots = new TreeMap<>();

    int pos = entryOffset;
    for (int i = 0; i < numEntries && pos <= limit - ENTRY_HEADER_SIZE; i++) {
      int entryLen = view.getInt(pos);
      if (entryLen < ENTRY_HEADER_SIZE || entryLen > limit - pos) {
        break;
      }
      int nameOffset = view.getInt(pos + 4);
      int vectorLen = view.getInt(pos + 8);
      byte dataType = view.get(pos + 12);
      int dataOffset = view.getInt(pos + 16);

      if (nameOffset >= ENTRY_HEADER_SIZE && nameOffset < entryLen && view.get(pos + nameOffset) == 's') {
        int nameMaxLen = (dataOffset > nameOffset && dataOffset <= entryLen)
          ? (dataOffset - nameOffset)
          : (entryLen - nameOffset);
        String entryName = readCString(view, pos + nameOffset, nameMaxLen);
        if (HRT_FREQUENCY_KEY.equals(entryName)) {
          if (dataType == 'J' && vectorLen == 0 && isValidLongOffset(dataOffset, entryLen)) {
            long freq = view.getLong(pos + dataOffset);
            if (freq > 0) {
              frequency = freq;
            }
          }
        } else if (entryName.startsWith(COLLECTOR_PREFIX)) {
          parseCollectorEntry(view, pos, entryLen, vectorLen, dataType, dataOffset, entryName, slots);
        }
      }
      pos += entryLen;
    }

    if (slots.isEmpty()) {
      return Collections.emptyList();
    }
    List<Collector> result = new ArrayList<>(slots.size());
    for (CollectorSlot slot : slots.values()) {
      if (slot.isComplete()) {
        result.add(new Collector(view, slot.name, slot.invocationsOffset, slot.timeOffset, frequency));
      }
    }
    return Collections.unmodifiableList(result);
  }

  private static void parseCollectorEntry(
      ByteBuffer view,
      int pos,
      int entryLen,
      int vectorLen,
      byte dataType,
      int dataOffset,
      String entryName,
      Map<Integer, CollectorSlot> slots) {

    int idxStart = COLLECTOR_PREFIX.length();
    int dot = entryName.indexOf('.', idxStart);
    if (dot <= idxStart) {
      return;
    }
    int collectorIndex = parseNonNegativeInt(entryName, idxStart, dot);
    if (collectorIndex < 0) {
      return;
    }
    String field = entryName.substring(dot + 1);
    if ("name".equals(field)) {
      if (dataType == 'B' && vectorLen > 0 && dataOffset >= ENTRY_HEADER_SIZE && dataOffset < entryLen) {
        int maxBytes = Math.min(vectorLen, entryLen - dataOffset);
        String collectorName = readCString(view, pos + dataOffset, maxBytes).trim();
        if (!collectorName.isEmpty()) {
          slots.computeIfAbsent(collectorIndex, k -> new CollectorSlot()).name = collectorName;
        }
      }
    } else if ("invocations".equals(field)) {
      if (dataType == 'J' && vectorLen == 0 && isValidLongOffset(dataOffset, entryLen)) {
        slots.computeIfAbsent(collectorIndex, k -> new CollectorSlot()).invocationsOffset = pos + dataOffset;
      }
    } else if ("time".equals(field)) {
      if (dataType == 'J' && vectorLen == 0 && isValidLongOffset(dataOffset, entryLen)) {
        slots.computeIfAbsent(collectorIndex, k -> new CollectorSlot()).timeOffset = pos + dataOffset;
      }
    }
  }

  private static boolean isValidLongOffset(int dataOffset, int entryLen) {
    return dataOffset >= ENTRY_HEADER_SIZE && dataOffset <= entryLen - Long.BYTES;
  }

  private static int parseNonNegativeInt(String s, int start, int end) {
    int value = 0;
    for (int i = start; i < end; i++) {
      char c = s.charAt(i);
      if (c < '0' || c > '9') {
        return -1;
      }
      value = value * 10 + (c - '0');
      if (value < 0) {
        return -1;
      }
    }
    return value;
  }

  private static String readCString(ByteBuffer view, int offset, int maxLen) {
    int len = 0;
    while (len < maxLen && view.get(offset + len) != 0) {
      len++;
    }
    if (len == 0) {
      return "";
    }
    byte[] bytes = new byte[len];
    for (int i = 0; i < len; i++) {
      bytes[i] = view.get(offset + i);
    }
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private static Set<Path> candidateTempDirs() {
    Set<Path> dirs = new LinkedHashSet<>(2);
    dirs.add(Paths.get("/tmp"));
    String tmpDir = System.getProperty("java.io.tmpdir");
    if (tmpDir != null && !tmpDir.isEmpty()) {
      dirs.add(Paths.get(tmpDir));
    }
    return dirs;
  }

  private static @Nullable Path findPerfDataFile(Set<Path> baseDirs, @Nullable String user, long pid) {
    String pidStr = Long.toString(pid);
    if (user != null && !user.isEmpty()) {
      for (Path base : baseDirs) {
        Path direct = base.resolve(PERFDATA_DIR_PREFIX + user).resolve(pidStr);
        if (Files.isRegularFile(direct)) {
          return direct;
        }
      }
    }
    for (Path base : baseDirs) {
      if (!Files.isDirectory(base)) {
        continue;
      }
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(base, PERFDATA_DIR_PREFIX + "*")) {
        for (Path dir : stream) {
          Path candidate = dir.resolve(pidStr);
          if (Files.isRegularFile(candidate)) {
            return candidate;
          }
        }
      } catch (IOException | RuntimeException e) {
        log.log(DEBUG, "Unable to scan hsperfdata directory in " + base, e);
      }
    }
    return null;
  }

  private static boolean isPerfDataInitializing(@Nullable Path existingFile) {
    if (existingFile != null) {
      return true;
    }
    try {
      ThreadGroup group = Thread.currentThread().getThreadGroup();
      if (group == null) {
        return false;
      }
      while (group.getParent() != null) {
        group = group.getParent();
      }
      Thread[] threads = new Thread[group.activeCount() + 8];
      int count = group.enumerate(threads, true);
      for (int i = 0; i < count; i++) {
        Thread thread = threads[i];
        if (thread != null && thread.isAlive() && PERFDATA_THREAD_NAME.equals(thread.getName())) {
          return true;
        }
      }
    } catch (RuntimeException e) {
      log.log(DEBUG, "Unable to inspect threads for Performance data initialization", e);
    }
    return false;
  }

  static long ticksToMillis(long ticks, long frequency) {
    if (ticks <= 0 || frequency <= 0) {
      return 0L;
    }
    if (frequency >= 1000L && frequency % 1000L == 0L) {
      return ticks / (frequency / 1000L);
    }
    return (ticks / frequency) * 1000L + ((ticks % frequency) * 1000L) / frequency;
  }

  private static final class CollectorSlot {
    String name = "";
    int invocationsOffset = -1;
    int timeOffset = -1;

    boolean isComplete() {
      return !name.isEmpty() && invocationsOffset >= 0 && timeOffset >= 0;
    }
  }

  static final class Collector {

    private final ByteBuffer buffer;
    private final String name;
    private final int invocationsOffset;
    private final int timeOffset;
    private final long frequency;

    Collector(ByteBuffer buffer, String name, int invocationsOffset, int timeOffset, long frequency) {
      this.buffer = buffer;
      this.name = name;
      this.invocationsOffset = invocationsOffset;
      this.timeOffset = timeOffset;
      this.frequency = frequency;
    }

    String name() {
      return name;
    }

    long frequency() {
      return frequency;
    }

    long collectionCount() {
      return Math.max(0L, buffer.getLong(invocationsOffset));
    }

    long collectionTimeTicks() {
      return Math.max(0L, buffer.getLong(timeOffset));
    }

    long collectionTimeMillis() {
      return ticksToMillis(collectionTimeTicks(), frequency);
    }
  }
}
