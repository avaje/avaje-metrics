package io.avaje.metrics.core.noop;

import io.avaje.metrics.Timer;

class NoopTimedEvent implements Timer.Event {

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
