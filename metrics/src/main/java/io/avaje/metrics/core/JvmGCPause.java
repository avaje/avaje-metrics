package io.avaje.metrics.core;

import com.sun.management.GarbageCollectionNotificationInfo;
import io.avaje.applog.AppLog;
import io.avaje.metrics.Meter;
import io.avaje.metrics.MetricRegistry;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sun.management.GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION;
import static java.lang.System.Logger.Level.INFO;

final class JvmGCPause {

  private static final System.Logger log = AppLog.getLogger("io.avaje.metrics");

  static void createMeters(MetricRegistry registry, boolean withDetails) {
    if (!extensionsPresent() || !hasNotifications()) {
      return;
    }
    final var jcPause = new JvmGCPause();
    jcPause.createMeters(registry);
  }

  // private final List<Runnable> cleanup = new ArrayList<>();

  void createMeters(MetricRegistry registry) {
    final var listener = new GcListener(registry);
    final var filter = new Filter();
    for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
      if (!(gcBean instanceof NotificationEmitter)) {
        continue;
      }
      final var emitter = (NotificationEmitter) gcBean;
      emitter.addNotificationListener(listener, filter, null);

//      cleanup.add(() -> {
//        try {
//          notificationEmitter.removeNotificationListener(listener);
//        } catch (ListenerNotFoundException ignore) {
//          // ignore
//        }
//      });
    }
  }
//
//  @Override
//  public void close() {
//    // cleanup.forEach(Runnable::run);
//  }

  static final class Filter implements NotificationFilter {
    @Override
    public boolean isNotificationEnabled(Notification notification) {
      return GARBAGE_COLLECTION_NOTIFICATION.equals(notification.getType());
    }
  }

  static final class GcListener implements NotificationListener {

    private final MetricRegistry registry;

    GcListener(MetricRegistry registry) {
      this.registry = registry;
    }

    @Override
    public void handleNotification(Notification notification, Object ref) {
      CompositeData cd = (CompositeData) notification.getUserData();
      GarbageCollectionNotificationInfo notificationInfo = GarbageCollectionNotificationInfo.from(cd);

      String gcName = notificationInfo.getGcName();
      String gcCause = notificationInfo.getGcCause();
      long duration = notificationInfo.getGcInfo().getDuration();

      //Tags tags = Tags.of("gc:" + gcName, "action:" + gcAction, "cause:" + gcCause);
      if (isConcurrentPhase(gcCause, gcName)) {
        Meter meter = registry.meter("jvm.gc.concurrent");//, tags);
        meter.addEvent(duration);

      } else {
        Meter meter = registry.meter("jvm.gc.pause");//, tags);
        meter.addEvent(duration);
      }
    }
  }

  private static boolean isConcurrentPhase(String cause, String name) {
    return "No GC".equals(cause)
      || "Shenandoah Cycles".equals(name)
      || (name.startsWith("ZGC") && name.endsWith("Cycles")) // ZGC
      || (name.startsWith("GPGC") && !name.endsWith("Pauses")) // Zing
      ;
  }

  private static boolean extensionsPresent() {
    if (ManagementFactory.getMemoryPoolMXBeans().isEmpty()) {
      // native-image Substrate VM
      log.log(INFO, "GC notifications not available, empty MemoryPoolMXBeans");
      return false;
    }

    try {
      Class.forName("com.sun.management.GarbageCollectionNotificationInfo", false,
        MemoryPoolMXBean.class.getClassLoader());
      return true;
    } catch (Throwable e) {
      // We are operating in a JVM without access to this level of detail
      log.log(INFO, "GC notifications not available - no com.sun.management.GarbageCollectionNotificationInfo");
      return false;
    }
  }

  private static boolean hasNotifications() {
    List<String> gcsWithoutNotification = new ArrayList<>();
    for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
      if (!(gcBean instanceof NotificationEmitter)) {
        continue;
      }
      final var emitter = (NotificationEmitter) gcBean;
      boolean notificationAvailable = Arrays.stream(emitter.getNotificationInfo())
        .anyMatch(notificationInfo -> Arrays.asList(notificationInfo.getNotifTypes())
          .contains(GARBAGE_COLLECTION_NOTIFICATION));
      if (notificationAvailable) {
        return true;
      }
      gcsWithoutNotification.add(gcBean.getName());
    }
    log.log(INFO, "GC notifications not available GCs=" + gcsWithoutNotification);
    return false;
  }
}
