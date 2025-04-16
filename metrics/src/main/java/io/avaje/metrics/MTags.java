package io.avaje.metrics;

import java.util.Arrays;
import java.util.Objects;

final class MTags implements Tags {

  static Tags EMPTY = new MTags(new String[]{});

  private final String[] keyValuePairs;

  MTags(String[] keyValuePairs) {
    if (keyValuePairs.length % 2 != 0) {
      throw new IllegalArgumentException("Incorrect length, must be pairs of key values");
    }
    this.keyValuePairs = keyValuePairs;
  }

  @Override
  public boolean isEmpty() {
    return keyValuePairs.length == 0;
  }

  @Override
  public String[] array() {
    return keyValuePairs;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof MTags)) return false;
    MTags dTags = (MTags) object;
    return Objects.deepEquals(keyValuePairs, dTags.keyValuePairs);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(keyValuePairs);
  }
}
