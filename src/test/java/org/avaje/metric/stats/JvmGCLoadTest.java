package org.avaje.metric.stats;

import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.avaje.metric.jvm.GarbageCollectionRateCollection;
import org.junit.Test;

public class JvmGCLoadTest {

  @Test
  public void test() throws InterruptedException {

    long start = System.currentTimeMillis();
    Timer timer = new Timer();
    GarbageCollectionRateCollection gc = new GarbageCollectionRateCollection(timer);

    for (int i = 0; i < 15; i++) {

      createSomeGarbage();
    }

    long exe = System.currentTimeMillis() - start;
    System.out.println("Duration " + exe + " millis");
    System.out.println("" + gc);

    Thread.sleep(10000);
  }

  private void createSomeGarbage() {

    Map<String, String> m = new ConcurrentHashMap<String, String>();

    Random r = new Random();

    for (int i = 0; i < 1500000; i++) {
      int nextInt = r.nextInt(10000000);

      String s = new String("" + nextInt);
      m.put(s, s);
    }

  }

}
