package io.avaje.metrics.core;

import io.avaje.metrics.MetricManager;
import io.avaje.metrics.Timer;
import io.avaje.metrics.TimerGroup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimedMetricGroupTest {

  @Test
  void test() {
    TimerGroup timedGroup = MetricManager.timerGroup("org.test.Hello");

    Timer timedMetric = timedGroup.timed("one");
    assertEquals("org.test.Hello.one", timedMetric.name());


    Timer timedMetric2 = timedGroup.timed("one");

    assertSame(timedMetric, timedMetric2);


    Timer two = timedGroup.timed("two");
    assertNotSame(timedMetric, two);

    assertEquals("org.test.Hello.two", two.name());
  }
}
