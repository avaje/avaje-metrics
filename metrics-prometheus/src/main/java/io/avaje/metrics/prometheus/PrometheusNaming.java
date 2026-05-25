package io.avaje.metrics.prometheus;

final class PrometheusNaming {

  private PrometheusNaming() {
  }

  static String metricName(String rawName) {
    if (rawName.isEmpty()) {
      return "metric";
    }
    var builder = new StringBuilder(rawName.length() + 2);
    for (int i = 0; i < rawName.length(); i++) {
      var ch = rawName.charAt(i);
      if (i == 0 && !isValidMetricStart(ch)) {
        builder.append("m_");
      }
      builder.append(isValidMetricChar(ch) ? ch : '_');
    }
    return builder.toString();
  }

  static String labelName(String rawName) {
    if (rawName.isEmpty()) {
      return "label";
    }
    var builder = new StringBuilder(rawName.length() + 2);
    for (int i = 0; i < rawName.length(); i++) {
      var ch = rawName.charAt(i);
      if (i == 0 && !isValidLabelStart(ch)) {
        builder.append('_');
      }
      builder.append(isValidLabelChar(ch) ? ch : '_');
    }
    return builder.toString();
  }

  static String counterName(String baseName) {
    return baseName.endsWith("_total") ? baseName : baseName + "_total";
  }

  static String secondsName(String baseName) {
    return baseName.endsWith("_seconds") ? baseName : baseName + "_seconds";
  }

  private static boolean isValidMetricStart(char ch) {
    return ch == '_' || ch == ':' || isAsciiAlpha(ch);
  }

  private static boolean isValidMetricChar(char ch) {
    return isValidMetricStart(ch) || isAsciiDigit(ch);
  }

  private static boolean isValidLabelStart(char ch) {
    return ch == '_' || isAsciiAlpha(ch);
  }

  private static boolean isValidLabelChar(char ch) {
    return isValidLabelStart(ch) || isAsciiDigit(ch);
  }

  private static boolean isAsciiAlpha(char ch) {
    return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
  }

  private static boolean isAsciiDigit(char ch) {
    return ch >= '0' && ch <= '9';
  }
}
