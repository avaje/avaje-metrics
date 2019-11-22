package io.avaje.metrics.core;

import io.avaje.metrics.RequestTiming;
import io.avaje.metrics.RequestTimingEntry;
import io.avaje.metrics.TimedMetric;
import io.avaje.metrics.report.BasicRequestTimingWriter;
import org.testng.annotations.Test;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class BasicRequestTimingWriterTest {

  BasicRequestTimingWriter requestTimingWriter = new BasicRequestTimingWriter();

  TimedMetric m0 = new DefaultTimedMetric(new DefaultMetricName("org.req.Customer.getById"));
  TimedMetric m1 = new DefaultTimedMetric(new DefaultMetricName("org.service.CustomerService.getById"));
  TimedMetric m2 = new DefaultTimedMetric(new DefaultMetricName("org.data.CustomerDAO.getById"));

  @Test
  public void testWrite() throws Exception {


    StringWriter writer = new StringWriter();
    BaseTimingEntry e0 = new BaseTimingEntry(0, m0, System.nanoTime());
    Thread.sleep(10);
    BaseTimingEntry e1 = new BaseTimingEntry(1, m1, System.nanoTime());
    Thread.sleep(20);
    BaseTimingEntry e2 = new BaseTimingEntry(2, m2, System.nanoTime());
    Thread.sleep(50);
    e2.setEndNanos(System.nanoTime());
    e1.setEndNanos(System.nanoTime());
    e0.setEndNanos(System.nanoTime());

    List<RequestTimingEntry> entries = new ArrayList<>();
    entries.add(e2);
    entries.add(e1);
    entries.add(e0);

    RequestTiming requestTiming = new DefaultRequestTiming(entries, System.currentTimeMillis());

    requestTimingWriter.writeEntry(writer, requestTiming);

    String output = writer.toString();

    System.out.println(output);

    assertTrue(output.contains("metric:org.req.Customer.getById"));
    assertTrue(output.contains("m:org.req.Customer.getById"));
    assertTrue(output.contains("m:org.service.CustomerService.getById"));
    assertTrue(output.contains("m:org.data.CustomerDAO.getById"));

  }

  @Test
  public void testToMillis() throws Exception {

    assertEquals(1L, toMillis(1000000L));
    assertEquals(0L, toMillis(900000L)); // rounds down
    assertEquals(1L, toMillis(1100000L));
    assertEquals(1L, toMillis(1500000L));
    assertEquals(1L, toMillis(1600000L));
    assertEquals(2L, toMillis(2000000L));
  }

  /**
   * Return the nanos as milliseconds.
   */
  protected static long toMillis(long nanos) {
    return TimeUnit.NANOSECONDS.toMillis(nanos);
  }
}
