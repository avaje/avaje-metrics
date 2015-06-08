package org.avaje.metric.core;

import org.avaje.metric.core.spi.ExternalRequestIdAdapter;
import org.slf4j.MDC;

/**
 * Obtains the external request id from MDC context.
 */
public class MdcExternalRequestIdAdapter implements ExternalRequestIdAdapter {

  final String key;

  /**
   * Construct supplying a MDC key.
   */
  public MdcExternalRequestIdAdapter(String key) {
    this.key = key;
  }

  /**
   * Return the external request id from the MDC context.
   */
  @Override
  public String getExternalRequestId() {
    return MDC.get(key);
  }
}
