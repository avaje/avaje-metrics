package org.avaje.metric.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Name mapping used to provide shorter metric names with common prefixes.
 */
public class NameMapping {

  private static final String METRIC_NAME_MAPPING_RESOURCE = "metric-name-mapping.txt";

  private final ClassLoader classLoader;

  private final Map<String, String> nameMapping;

  private final String[] metricNameMatches;

  /**
   * Create with a classLoader.
   * <p>
   * The classLoader is used for loading metric-name-mapping.txt resources (if any).
   * </p>
   */
  public NameMapping(ClassLoader classLoader) {
    this.classLoader = classLoader;
    this.nameMapping = readNameMapping();
    this.metricNameMatches = getMetricNameMatches();
  }

  public String getMatches() {
    return Arrays.toString(this.metricNameMatches);
  }

  /**
   * Return a potentially cut down metric name.
   * <p>
   * For example, trim of extraneous package names or prefix controllers or
   * JAX-RS endpoints with "web" etc.
   * </p>
   */
  public String getMappedName(String rawName) {

    // search for a match in reverse order
    for (int i = metricNameMatches.length-1; i >= 0; i--) {
      String name = metricNameMatches[i];
      if (rawName.startsWith(name)) {
        String prefix = nameMapping.get(name);
        if (prefix == null || prefix.length() == 0) {
          // trim without any prefix
          return trimMetricName(rawName.substring(name.length()));

        } else {
          return trimMetricName(prefix + rawName.substring(name.length()));
        }
      }
    }
    return rawName;
  }

  /**
   * trim off any leading period.
   */
  private String trimMetricName(String metricName) {
    if (metricName.startsWith(".")) {
      return metricName.substring(1);
    }
    return metricName;
  }

  /**
   * Return the keys in order.
   */
  private String[] getMetricNameMatches() {

    List<String> keys = new ArrayList<>();
    keys.addAll(this.nameMapping.keySet());
    Collections.sort(keys);
    return keys.toArray(new String[keys.size()]);
  }

  /**
   * Return all the metric-name-mapping.txt resources (if any).
   */
  private Enumeration<URL> getNameMappingResources() throws IOException {

    if (classLoader != null) {
      return classLoader.getResources(METRIC_NAME_MAPPING_RESOURCE);
    } else {
      return getClass().getClassLoader().getResources(METRIC_NAME_MAPPING_RESOURCE);
    }
  }

  /**
   * Return all the mappings as a Map.
   */
  private Map<String,String> readNameMapping() {

    Map<String,String> map = new HashMap<>();

    try {
      Enumeration<URL> resources = getNameMappingResources();
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        InputStream inStream = url.openStream();
        try {
          Properties props = new Properties();
          props.load(inStream);

          Set<String> stringPropertyNames = props.stringPropertyNames();
          for (String propName : stringPropertyNames) {
            map.put(propName, props.getProperty(propName));
          }
        } finally {
          if (inStream != null) {
            inStream.close();
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Error trying to read metric-name-mapping.properties resources");
      e.printStackTrace();
    }
    return map;
  }
}
