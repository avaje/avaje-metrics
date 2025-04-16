package io.avaje.metrics;

public interface Tags {

  Tags EMPTY = MTags.EMPTY;

  static Tags of() {
    return EMPTY;
  }

  static Tags of(String... keyValuePairs) {
    return new MTags(keyValuePairs);
  }

  String[] array();

  boolean isEmpty();
}
