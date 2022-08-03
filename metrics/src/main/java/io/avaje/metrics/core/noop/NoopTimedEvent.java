package io.avaje.metrics.core.noop;

import io.avaje.metrics.TimedEvent;

class NoopTimedEvent implements TimedEvent {

  @Override
  public void end(boolean withSuccess) {
    // do nothing
  }

  @Override
  public void end() {
    // do nothing
  }

  @Override
  public void endWithError() {
    // do nothing
  }

}