package io.avaje.metrics;

import java.util.concurrent.TimeUnit;

/**
 * No-op {@link ScheduledTask}. All operations are no-ops; nothing is scheduled
 * and no executor resources are held. Returned by {@link ScheduledTask#noop()}.
 */
final class NoopScheduledTask implements ScheduledTask {

  static final NoopScheduledTask INSTANCE = new NoopScheduledTask();

  private NoopScheduledTask() {
  }

  @Override
  public void start() {
    // no-op
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public void waitIfRunning(long timeout, TimeUnit timeUnit) {
    // no-op
  }
}
