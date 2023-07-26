package io.avaje.metrics.report;

import java.text.DecimalFormat;

/**
 * Utility for formatting numbers.
 */
public class NumFormat {

  private static DecimalFormat format4 = new DecimalFormat("0.0###");
  private static DecimalFormat format3 = new DecimalFormat("0.0##");
  private static DecimalFormat format2 = new DecimalFormat("0.0#");
  private static DecimalFormat format1 = new DecimalFormat("0.0");
  private static DecimalFormat format0 = new DecimalFormat("0");

  /**
   * Format to the number of decimal places.
   *
   * @param decimalPlaces supports 0 to 4 decimal places.
   * @param number        the value
   * @return the decimal formatted value
   */
  public static String dp(int decimalPlaces, double number) {
    switch (decimalPlaces) {
      case 0:
        return zerodp(number);
      case 1:
        return onedp(number);
      case 2:
        return twodp(number);
      case 3:
        return threedp(number);
      case 4:
        return fourdp(number);

      default:
        throw new IllegalArgumentException(decimalPlaces + " decimal places not supported");
    }
  }

  /**
   * Format the number to 0 decimal places.
   */
  public static String zerodp(double number) {
    return format0.format(number);
  }

  /**
   * Format the number to 1 decimal places.
   */
  public static String onedp(double number) {
    return format1.format(number);
  }

  /**
   * Format the number to 2 decimal places.
   */
  public static String twodp(double number) {
    return format2.format(number);
  }

  /**
   * Format the number to 3 decimal places.
   */
  public static String threedp(double number) {
    return format3.format(number);
  }

  /**
   * Format the number to 4 decimal places.
   */
  public static String fourdp(double number) {
    return format4.format(number);
  }
}
