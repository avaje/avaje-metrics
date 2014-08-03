package org.avaje.metric.core.noop;

import org.avaje.metric.TimedEvent;

class NoopTimedEvent implements TimedEvent {

  @Override
  public void end(boolean withSuccess) {
    // do nothing
  }

  @Override
  public void endWithSuccess() {
    // do nothing
  }

  @Override
  public void endWithError() {
    // do nothing
  }
  
}