package io.avaje.metrics.core;

import io.avaje.applog.AppLog;
import io.avaje.metrics.GaugeLong;
import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.Tags;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.LongSupplier;

import static java.lang.System.Logger.Level.INFO;

/**
 * Collect statistics on the count and pause time of garbage collection.
 */
final class JvmGarbageCollection {

  private static final System.Logger log = AppLog.getLogger("io.avaje.metrics");
  private static final String GC_COUNT = "jvm.gc.count";
  private static final String GC_TIME = "jvm.gc.time";

  static void createGauges(MetricRegistry registry, Tags globalTags) {
    List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    if (!gcMXBeans.isEmpty()) {
      registerMxBeans(registry, gcMXBeans, globalTags);
      return;
    }
    List<HsPerfDataGc.Collector> perfCollectors = HsPerfDataGc.discover();
    if (!perfCollectors.isEmpty()) {
      registerPerfCollectors(registry, perfCollectors, globalTags);
      return;
    }
    log.log(INFO, "GC metrics not available, empty GarbageCollectorMXBeans (for GraalVM G1 use --enable-monitoring=jvmstat)");
  }

  static void registerMxBeans(MetricRegistry registry, List<GarbageCollectorMXBean> gcMXBeans, Tags globalTags) {
    Set<String> usedTags = new HashSet<>();
    for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
      Tags tags = collectorTags(globalTags, gcMXBean.getName(), usedTags);
      registry.gauge(GC_COUNT).tags(tags).ofLongs(GaugeLong.incrementing(new Count(gcMXBean)));
      registry.gauge(GC_TIME).tags(tags).ofLongs(GaugeLong.incrementing(new Time(gcMXBean)));
    }
  }

  static void registerPerfCollectors(MetricRegistry registry, List<HsPerfDataGc.Collector> collectors, Tags globalTags) {
    Set<String> usedTags = new HashSet<>();
    for (HsPerfDataGc.Collector collector : collectors) {
      Tags tags = collectorTags(globalTags, collector.name(), usedTags);
      registry.gauge(GC_COUNT).tags(tags).ofLongs(GaugeLong.incrementing(collector::collectionCount));
      registry.gauge(GC_TIME).tags(tags).ofLongs(GaugeLong.incrementing(collector::collectionTimeMillis));
    }
  }

  static Tags collectorTags(Tags globalTags, String collectorName, Set<String> usedTags) {
    String gc = gcTag(collectorName);
    if (!usedTags.add(gc)) {
      gc = normalizeGcName(collectorName);
      usedTags.add(gc);
    }
    return Tags.of(globalTags.append("gc:" + gc));
  }

  static String gcTag(String collectorName) {
    String lower = collectorName.trim().toLowerCase();
    switch (lower) {
      case "g1 young generation":
      case "g1 young collection pauses":
      case "serial young collection pauses":
      case "young generation scavenger":
      case "ps scavenge":
      case "copy":
      case "parnew":
        return "young";
      case "g1 old generation":
      case "g1 full collection pauses":
      case "serial full collection pauses":
      case "complete scavenger":
      case "ps marksweep":
      case "marksweepcompact":
        return "full";
      case "g1 concurrent gc":
      case "g1 concurrent cycle pauses":
      case "concurrentmarksweep":
      case "shenandoah cycles":
      case "zgc cycles":
        return "concurrent";
      default:
        return normalizeGcName(lower);
    }
  }

  static String normalizeGcName(String collectorName) {
    return collectorName.trim().toLowerCase().replace(' ', '_').replace(".", "");
  }

  private static final class Count implements LongSupplier {

    private final GarbageCollectorMXBean gcMXBean;

    Count(GarbageCollectorMXBean gcMXBean) {
      this.gcMXBean = gcMXBean;
    }

    @Override
    public long getAsLong() {
      return gcMXBean.getCollectionCount();
    }
  }

  private static final class Time implements LongSupplier {

    private final GarbageCollectorMXBean gcMXBean;

    Time(GarbageCollectorMXBean gcMXBean) {
      this.gcMXBean = gcMXBean;
    }

    @Override
    public long getAsLong() {
      return gcMXBean.getCollectionTime();
    }
  }
}
