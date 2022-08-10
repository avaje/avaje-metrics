package io.avaje.metrics.core;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

class JvmGCLoadTest {

  //@Ignore
  @Test
  void test() throws InterruptedException {
    DefaultMetricProvider registry = new DefaultMetricProvider();
    JvmGarbageCollectionMetricGroup.createGauges(registry, true);
    for (int i = 0; i < 3; i++) {
      doSomething(registry);
    }
    Thread.sleep(100);
  }

  private void doSomething(DefaultMetricProvider registry) {
    long start = System.currentTimeMillis();
    for (int i = 0; i < 2; i++) {
      createSomeGarbage();
    }

    long exe = System.currentTimeMillis() - start;
    System.out.println("Duration " + exe + " millis");
    System.out.println(registry);
  }

  private void createSomeGarbage() {
    Map<String, String> map = new ConcurrentHashMap<>();
    Random r = new Random();
    for (int i = 0; i < 1000000; i++) {
      int nextInt = r.nextInt(10000000);
      String s = "" + nextInt;
      map.put(s, s);
    }
  }

}
