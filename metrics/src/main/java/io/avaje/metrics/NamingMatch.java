package io.avaje.metrics;

import java.util.function.Function;

/**
 * Naming convention that is just exact match.
 * <p>
 * This is the default naming convention used.
 */
public final class NamingMatch implements Function<String, String> {

  /**
   * Return this exact match naming convention.
   */
  public static final Function<String, String> INSTANCE = new NamingMatch();

  private NamingMatch() {
    // hide constructor
  }

  @Override
  public String apply(String name) {
    return name;
  }
}
