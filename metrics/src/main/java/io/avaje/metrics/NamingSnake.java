package io.avaje.metrics;

import java.util.function.Function;

/**
 * Naming convention that replaces period with underscore.
 */
public final class NamingSnake implements Function<String, String> {

  @Override
  public String apply(String name) {
    return name.replace('.', '_');
  }
}
