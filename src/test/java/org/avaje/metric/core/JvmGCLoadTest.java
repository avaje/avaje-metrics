package org.avaje.metric.core;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.avaje.metric.GaugeCounterMetricGroup;
import org.avaje.metric.jvm.JvmGarbageCollectionMetricGroup;
import org.junit.Test;

public class JvmGCLoadTest {

  //@Ignore
  @Test
  public void test() throws InterruptedException {

    GaugeCounterMetricGroup[] gaugeMetricGroups = JvmGarbageCollectionMetricGroup.createGauges();
    
    for (int i = 0; i < 3; i++) {
      doSomething(gaugeMetricGroups);      
    }

    Thread.sleep(3000);
  }

  private void doSomething(GaugeCounterMetricGroup[] gaugeMetricGroups) {
    
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

    for (int i = 0; i < 1000000; i++) {
      int nextInt = r.nextInt(10000000);

      String s = new String("" + nextInt);
      m.put(s, s);
    }

  }

}
