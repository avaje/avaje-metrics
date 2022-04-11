package io.avaje.metrics.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

/**
 * Configuration information for metrics such as memory warning levels.
 */
public class MetricManifest {

  private static final Logger logger = LoggerFactory.getLogger(MetricManifest.class);

  private static final MetricManifest INSTANCE = read("metrics.mf");

  private final Manifest manifest;

  /**
   * Return the default instance.
   */
  public static MetricManifest get() {
    return INSTANCE;
  }

  /**
   * Create with a manifest.
   */
  public MetricManifest(Manifest manifest) {
    this.manifest = manifest;
  }

  /**
   * Return true if there is a memory warning level set.
   */
  public boolean hasMemoryWarning() {
    return getMemoryWarnRelative() > 0 || getMemoryWarnAbsolute() > 0;
  }

  /**
   * Return the absolute memory is the warning level (in MB).
   */
  public long getMemoryWarnAbsolute() {
    long general = valueLong("memory-warn-absolute", 0);
    return valueLong("app-memory-warn-absolute", general);
  }

  /**
   * Return the relative memory above Max Heap + Max Non Heap that is the warning level (in MB).
   */
  public long getMemoryWarnRelative() {
    long general = valueLong("memory-warn-relative", 0);
    return valueLong("app-memory-warn-relative", general);
  }

  /**
   * Return true if the process memory metric is disabled.
   */
  public boolean disableProcessMemory() {
    return valueBool("disable-process-memory", false);
  }

  /**
   * Return the maximum frequency a memory alert is issued in seconds.
   */
  public long getMemoryWarnFrequency() {
    return valueLong("memory-warn-frequency", 300);
  }

  private long valueLong(String key, long defaultValue) {
    String value = manifest.getMainAttributes().getValue(key);
    if (value != null) {
      try {
        return Long.parseLong(value);
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  private boolean valueBool(String key, boolean defaultValue) {
    String value = manifest.getMainAttributes().getValue(key);
    if (value != null) {
      return Boolean.parseBoolean(value);
    }
    return defaultValue;
  }

  static MetricManifest read(String resourceName) {
    Manifest manifest = new Manifest();
    ClassLoader classLoader = MetricManifest.class.getClassLoader();
    try {
      Enumeration<URL> manifests = classLoader.getResources(resourceName);
      while (manifests.hasMoreElements()) {
        URL url = manifests.nextElement();
        manifest.read(url.openStream());
      }
    } catch (IOException e) {
      logger.error("Error reading metric.mf manifest file?", e);
    }
    return new MetricManifest(manifest);
  }

}
