package org.avaje.metric;

import java.text.DecimalFormat;

/**
 * Utility for formatting numbers.
 */
public class NumFormat {

  private static DecimalFormat format4 = new DecimalFormat("0.0###");
  
  /**
   * Format the number to 4 decimal places.
   */
  public static String fourdp(double number){
    return format4.format(number);
  }
}
