package io.avaje.metrics;

import java.util.function.Function;

/**
 * Naming convention that is just exact match.
 */
public final class NamingMatch implements Function<String, String> {

  public static final Function<String, String> INSTANCE = new NamingMatch();

  @Override
  public String apply(String name) {
    return name;
  }
}
