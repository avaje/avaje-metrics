package io.avaje.metrics.statsd;

final class Trim {

  static String trim(String name, int prefix) {
    return name.substring(prefix);
  }

  static String qry(String name) {
    if (name.length() < 100) {
      return name.substring(4);
    }
    return name.substring(4, 99) + '_';
  }
}
