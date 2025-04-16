package io.avaje.metrics;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

final class MId implements Metric.ID {

  private final String name;
  private final Tags tags;

  MId(String name, Tags tags) {
    this.name = name;
    this.tags = tags;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Tags tags() {
    return tags;
  }

  @Override
  public Metric.ID suffix(String suffix) {
    return new MId(name + requireNonNull(suffix), tags);
  }

  @Override
  public Metric.ID withName(String otherName) {
    if (name.equals(requireNonNull(otherName))) {
      return this;
    }
    return new MId(otherName, tags);
  }

  @Override
  public Metric.ID withTags(Tags otherTags) {
    if (tags.equals(requireNonNull(otherTags))) {
      return this;
    }
    return new MId(name, otherTags);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof MId)) return false;
    MId key = (MId) object;
    return Objects.equals(name, key.name) && Objects.equals(tags, key.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, tags);
  }

  @Override
  public String toString() {
    return tags.isEmpty() ? name : name + ' ' + tags;
  }
}
