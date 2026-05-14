package io.avaje.metrics.spi;

/**
 * SPI for a span started by a traced timer event.
 */
public interface SpiTimedSpan {

  /**
   * End the span successfully.
   */
  void end();

  /**
   * End the span with error status.
   */
  void endWithError();
}
