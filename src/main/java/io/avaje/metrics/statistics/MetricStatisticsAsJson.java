package io.avaje.metrics.statistics;

public interface MetricStatisticsAsJson {

  /**
   * Write the metrics in JSON form to the given appendable.
   * <p>
   * Note that this doesn't add JSON array start and end so
   * those need to be added as needed.
   * </p>
   *
   * @param appendable The buffer to write the metrics to
   */
  void write(Appendable appendable);

  /**
   * Collect and return the metrics as JSON array content.
   */
  String asJson();
}
