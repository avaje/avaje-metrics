package org.avaje.metric;

import java.text.DecimalFormat;

/**
 * Utility for formatting numbers.
 */
public class NumFormat {

  private static DecimalFormat format4 = new DecimalFormat("0.0###");

  private static DecimalFormat format1 = new DecimalFormat("0.0");

  /**
   * Format the number to 1 decimal places.
   */
  public static String onedp(double number) {
    return format1.format(number);
  }

  /**
   * Format the number to 4 decimal places.
   */
  public static String fourdp(double number) {
    return format4.format(number);
  }
}
