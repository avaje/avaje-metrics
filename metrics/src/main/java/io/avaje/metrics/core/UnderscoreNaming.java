package io.avaje.metrics.core;

import java.util.function.Function;

/**
 * Naming convention that replaces period with underscore.
 */
final class UnderscoreNaming implements Function<String, String> {

  @Override
  public String apply(String name) {
    return name.replace('.', '_');
  }
}
