package io.avaje.metrics.spi;

/**
 * A started span that can later be ended with success or error.
 */
public interface SpiSpan {

  /**
   * End the span successfully.
   */
  void end();

  /**
   * End the span with error status.
   */
  void endWithError();

  /**
   * End the span with error status and record the Throwable when supported.
   */
  default void endWithError(Throwable error) {
    endWithError();
  }
}
