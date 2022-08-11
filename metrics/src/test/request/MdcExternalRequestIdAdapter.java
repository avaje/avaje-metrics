package io.avaje.metrics.core;

import io.avaje.metrics.core.spi.ExternalRequestIdAdapter;
//import org.slf4j.MDC;

/**
 * Obtains the external request id from MDC context.
 */
final class MdcExternalRequestIdAdapter implements ExternalRequestIdAdapter {

  final String key;

  /**
   * Construct supplying a MDC key.
   */
  MdcExternalRequestIdAdapter(String key) {
    this.key = key;
  }

  /**
   * Return the external request id from the MDC context.
   */
  @Override
  public String getExternalRequestId() {
    throw new IllegalArgumentException("Not supported at the moment");
    //return MDC.get(key);
  }
}
