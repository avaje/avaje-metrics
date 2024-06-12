package io.avaje.metrics;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduledTaskTest {

  @Test
  void runIt() throws InterruptedException {

    ScheduledTask task = ScheduledTask.builder()
      .schedule(1, 1, TimeUnit.MILLISECONDS)
      .task(ScheduledTaskTest::hello)
      .build();

    assertThat(counter.get()).isEqualTo(0);
    System.out.println("start...");
    task.start();
    Thread.sleep(5);
    assertThat(counter.get()).isGreaterThan(0);

    task.waitIfRunning(10, TimeUnit.SECONDS);
    Thread.sleep(30);

    System.out.println("cancel...");
    task.cancel(false);
    long after0 = counter.get();
    assertThat(after0).isGreaterThan(1);
    Thread.sleep(10);
    long after1 = counter.get();
    assertThat(after1).isEqualTo(after0);
    System.out.println("done");
  }

  private static final AtomicLong counter = new AtomicLong();

  private static void hello() {
    System.out.println("hi " + counter.incrementAndGet());
  }
}
