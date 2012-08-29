package org.avaje.metric.stats;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.avaje.metric.GaugeMetricGroup;
import org.avaje.metric.jvm.JvmGarbageCollectionMetricGroup;
import org.junit.Test;

public class JvmGCLoadTest {

  @Test
  public void test() throws InterruptedException {

    GaugeMetricGroup[] gaugeMetricGroups = JvmGarbageCollectionMetricGroup.createGauges();
    
    for (int i = 0; i < 10; i++) {
      doSomething(gaugeMetricGroups);      
    }

    Thread.sleep(10000);
  }

  private void doSomething(GaugeMetricGroup[] gaugeMetricGroups) {
    
    long start = System.currentTimeMillis();
    
    for (int i = 0; i < 15; i++) {

      createSomeGarbage();
    }

    long exe = System.currentTimeMillis() - start;
    System.out.println("Duration " + exe + " millis");
    System.out.println("" + Arrays.toString(gaugeMetricGroups));
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
