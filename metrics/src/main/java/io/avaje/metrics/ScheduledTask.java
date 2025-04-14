package io.avaje.metrics;

import java.util.concurrent.TimeUnit;

/**
 * A ScheduledTask that can run periodically, be cancelled, and
 * also aware of when its running (for use with Lambda).
 * <p>
 * The ScheduledTask will use a Daemon thread and expect to just
 * stop on JVM shutdown.
 */
public interface ScheduledTask {

  /**
   * Return the Builder for a ScheduledTask.
   */
  static Builder builder() {
    return new DScheduledTask.DBuilder();
  }

  /**
   * Start the scheduled task.
   */
  void start();

  /**
   * Cancel the scheduled task.
   * @return true if the task was cancelled otherwise false
   */
  boolean cancel(boolean mayInterruptIfRunning);

  /**
   * If the task is actively running wait for the task to complete.
   */
  void waitIfRunning(long timeout, TimeUnit timeUnit);

  /**
   * The builder for a ScheduledTask.
   */
  interface Builder {

    /**
     * Specify the schedule to run the task.
     * @param initial The initial delay
     * @param delay The delay between task execution
     * @param timeUnit The timeunit of the scheduled delay
     */
    Builder schedule(int initial, int delay, TimeUnit timeUnit);

    /**
     * Specify the task to execute periodically according to the schedule.
     */
    Builder task(Runnable task);

    /**
     * Build the scheduled task.
     */
    ScheduledTask build();
  }
}
