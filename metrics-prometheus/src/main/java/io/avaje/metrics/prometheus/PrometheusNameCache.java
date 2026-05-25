package io.avaje.metrics.prometheus;

import io.avaje.metrics.Tags;

import java.util.concurrent.ConcurrentHashMap;

final class PrometheusNameCache {

  private final ConcurrentHashMap<String, String> counterNames = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, String> gaugeNames = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, String> labelBlocks = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, String> labelBlocksWithExtra = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, MeterNames> meterNames = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, TimerNames> timerNames = new ConcurrentHashMap<>();

  String counter(String rawName) {
    return counterNames.computeIfAbsent(rawName, name -> PrometheusNaming.counterName(PrometheusNaming.metricName(name)));
  }

  String gauge(String rawName) {
    return gaugeNames.computeIfAbsent(rawName, PrometheusNaming::metricName);
  }

  String labels(Tags tags) {
    if (tags.isEmpty()) {
      return "";
    }
    return labelBlocks.computeIfAbsent(tags.cacheKey(), key -> buildLabels(tags, null, null));
  }

  String labels(Tags tags, String extraLabelName, String extraLabelValue) {
    var key = tags.cacheKey() + '\n' + extraLabelName + '\n' + extraLabelValue;
    return labelBlocksWithExtra.computeIfAbsent(key, ignore -> buildLabels(tags, extraLabelName, extraLabelValue));
  }

  MeterNames meter(String rawName) {
    return meterNames.computeIfAbsent(rawName, MeterNames::new);
  }

  TimerNames timer(String rawName) {
    return timerNames.computeIfAbsent(rawName, TimerNames::new);
  }

  private static String buildLabels(Tags tags, String extraLabelName, String extraLabelValue) {
    var rawTags = tags.array();
    var hasExtraLabel = extraLabelName != null;
    if (rawTags.length == 0 && !hasExtraLabel) {
      return "";
    }
    var labels = new StringBuilder(tags.cacheKey().length() + 16);
    for (var tag : rawTags) {
      if (tag == null) {
        continue;
      }
      var colon = tag.indexOf(':');
      if (colon <= 0) {
        continue;
      }
      var labelName = PrometheusNaming.labelName(tag.substring(0, colon));
      if (labelName.equals(extraLabelName)) {
        labelName = "tag_" + labelName;
      }
      appendLabel(labels, labelName, tag.substring(colon + 1));
    }
    if (hasExtraLabel) {
      appendLabel(labels, extraLabelName, extraLabelValue);
    }
    return labels.length() == 0 ? "" : "{" + labels + "}";
  }

  private static void appendLabel(StringBuilder labels, String name, String value) {
    if (labels.length() > 0) {
      labels.append(',');
    }
    labels.append(name).append("=\"");
    appendEscapedLabelValue(labels, value);
    labels.append('"');
  }

  private static void appendEscapedLabelValue(StringBuilder labels, String value) {
    for (int i = 0; i < value.length(); i++) {
      var ch = value.charAt(i);
      if (ch == '\\') {
        labels.append("\\\\");
      } else if (ch == '"') {
        labels.append("\\\"");
      } else if (ch == '\n') {
        labels.append("\\n");
      } else {
        labels.append(ch);
      }
    }
  }

  static final class MeterNames {

    private final String count;
    private final String total;
    private final String max;

    private MeterNames(String rawName) {
      var baseName = PrometheusNaming.metricName(rawName);
      this.count = PrometheusNaming.counterName(baseName + "_count");
      this.total = PrometheusNaming.counterName(baseName);
      this.max = baseName + "_max";
    }

    String count() {
      return count;
    }

    String total() {
      return total;
    }

    String max() {
      return max;
    }
  }

  static final class TimerNames {

    private final String base;
    private final String bucket;
    private final String count;
    private final String sum;
    private final String max;

    private TimerNames(String rawName) {
      this.base = PrometheusNaming.secondsName(PrometheusNaming.metricName(rawName));
      this.bucket = base + "_bucket";
      this.count = base + "_count";
      this.sum = base + "_sum";
      this.max = base + "_max";
    }

    String base() {
      return base;
    }

    String bucket() {
      return bucket;
    }

    String count() {
      return count;
    }

    String sum() {
      return sum;
    }

    String max() {
      return max;
    }
  }
}
