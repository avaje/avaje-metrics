package io.avaje.metrics.core;

import io.avaje.metrics.MetricManager;
import io.avaje.metrics.MetricName;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.TimedMetricGroup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimedMetricGroupTest {

  @Test
  void test() {
    TimedMetricGroup timedGroup = MetricManager.timedGroup(MetricName.of("org.test.Hello"));

    TimedMetric timedMetric = timedGroup.timed("one");
    assertEquals("org.test.Hello.one", timedMetric.name().simpleName());


    TimedMetric timedMetric2 = timedGroup.timed("one");

    assertSame(timedMetric, timedMetric2);


    TimedMetric two = timedGroup.timed("two");
    assertNotSame(timedMetric, two);

    assertEquals("org.test.Hello.two", two.name().simpleName());
  }
}
