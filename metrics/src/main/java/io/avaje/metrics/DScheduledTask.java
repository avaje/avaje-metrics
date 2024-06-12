package io.avaje.metrics;

import io.avaje.applog.AppLog;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.ERROR;

final class DScheduledTask implements ScheduledTask {

  private static final System.Logger log = AppLog.getLogger(DScheduledTask.class);

  static final class DBuilder implements Builder {
    private int initial = 60;
    private int delay = 60;
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    private Runnable task;

    @Override
    public DBuilder schedule(int initial, int delay, TimeUnit timeUnit) {
      this.initial = initial;
      this.delay = delay;
      this.timeUnit = timeUnit;
      return this;
    }

    @Override
    public DBuilder task(Runnable task) {
      this.task = task;
      return this;
    }

    @Override
    public DScheduledTask build() {
      Objects.requireNonNull(task, "task is required");
      return new DScheduledTask(task, initial, delay, timeUnit);
    }
  }


  private final ReentrantLock activeLock = new ReentrantLock();
  private final Runnable task;
  private final int initial;
  private final int delay;
  private final TimeUnit timeUnit;

  private final ScheduledExecutorService executor;
  private ScheduledFuture<?> backgroundTask;

  DScheduledTask(Runnable task, int initial, int delay, TimeUnit timeUnit) {
    this.task = task;
    this.initial = initial;
    this.delay = delay;
    this.timeUnit = timeUnit;
    this.executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("schTask"));
  }

  @Override
  public void start() {
    this.backgroundTask = executor.scheduleWithFixedDelay(this::runTask, initial, delay, timeUnit);
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return this.backgroundTask.cancel(mayInterruptIfRunning);
  }

  /**
   * Wait for the task to complete if it is actively running .
   */
  @Override
  public void waitIfRunning(long timeout, TimeUnit timeUnit) {
    try {
      if (activeLock.tryLock(timeout, timeUnit)) {
        activeLock.unlock();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.log(ERROR, "interrupted while waiting for task to complete", e);
    }
  }

  private void runTask() {
    activeLock.lock();
    try {
      task.run();
    } catch (Throwable e) {
      log.log(ERROR, "Error stopping task", e);
    } finally {
      activeLock.unlock();
    }
  }

  private static final class DaemonThreadFactory implements ThreadFactory {

    private final String name;

    DaemonThreadFactory(String name) {
      this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r, name);
      t.setDaemon(true);
      return t;
    }
  }
}
