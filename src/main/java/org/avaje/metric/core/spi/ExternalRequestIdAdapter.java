package org.avaje.metric.core.spi;

/**
 * Adapter that can be implemented to supply an external request id when reporting request timings.
 */
public interface ExternalRequestIdAdapter {

  String getExternalRequestId();
}
