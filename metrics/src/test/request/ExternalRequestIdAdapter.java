package io.avaje.metrics.core.spi;

/**
 * Adapter that can be implemented to supply an external request id when reporting request timings.
 */
public interface ExternalRequestIdAdapter {

  String getExternalRequestId();
}
