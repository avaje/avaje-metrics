package io.avaje.metrics.core;

import io.avaje.metrics.Metric;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class JvmGCLoadTest {

  //@Ignore
  @Test
  public void test() throws InterruptedException {

    List<Metric> metrics = JvmGarbageCollectionMetricGroup.createGauges(true);
    for (int i = 0; i < 3; i++) {
      doSomething(metrics);
    }

    Thread.sleep(100);
  }

  private void doSomething(List<Metric> metrics) {

    long start = System.currentTimeMillis();

    for (int i = 0; i < 2; i++) {

      createSomeGarbage();
    }

    long exe = System.currentTimeMillis() - start;
    System.out.println("Duration " + exe + " millis");
    System.out.println(metrics);
  }

  private void createSomeGarbage() {

    Map<String, String> m = new ConcurrentHashMap<>();

    Random r = new Random();

    for (int i = 0; i < 1000000; i++) {
      int nextInt = r.nextInt(10000000);

      String s = new String("" + nextInt);
      m.put(s, s);
    }

  }

}
