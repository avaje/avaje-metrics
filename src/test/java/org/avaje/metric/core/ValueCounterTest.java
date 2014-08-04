package org.avaje.metric.core;

import org.junit.Assert;
import org.junit.Test;

public class ValueCounterTest {

  @Test
  public void test() {
    
    ValueCounter counter = new ValueCounter();
    
    Assert.assertEquals(0, counter.getCount());
    Assert.assertEquals(0, counter.getTotal());
    Assert.assertEquals(Long.MIN_VALUE, counter.getMax());

    counter.add(100);
    Assert.assertEquals(1, counter.getCount());
    Assert.assertEquals(100, counter.getTotal());
    Assert.assertEquals(100, counter.getMax());

    counter.add(50);
    Assert.assertEquals(2, counter.getCount());
    Assert.assertEquals(150, counter.getTotal());
    Assert.assertEquals(100, counter.getMax());

    counter.add(200);
    Assert.assertEquals(3, counter.getCount());
    Assert.assertEquals(350, counter.getTotal());
    Assert.assertEquals(200, counter.getMax());

    counter.add(20);
    Assert.assertEquals(4, counter.getCount());
    Assert.assertEquals(370, counter.getTotal());
    Assert.assertEquals(200, counter.getMax());
    
    counter.reset();
    Assert.assertEquals(0, counter.getCount());
    Assert.assertEquals(0, counter.getTotal());
    Assert.assertEquals(Long.MIN_VALUE, counter.getMax());
    
  }
}
