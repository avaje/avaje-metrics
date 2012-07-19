package org.avaje.metric.stats;

import junit.framework.Assert;

import org.avaje.metric.Stats.Summary;
import org.junit.Test;

public class SummaryMovingBufferTest {

  @Test
  public void testBuffer() {
    
    long startTime = System.currentTimeMillis() - 10*60000;
    long ONE_MIN = 60000;
    
    SummaryMovingBuffer buffer = new SummaryMovingBuffer(3);
    
    Summary mInitial = buffer.getMovingAggregate(0);
    Assert.assertEquals(0, mInitial.getCount());
    
    StatsSum s0 = new StatsSum();
    buffer.put(s0);
    
    Summary m0 = buffer.getMovingAggregate(0);
    Assert.assertEquals(0, m0.getCount());
    
    StatsSum s1 = new StatsSum(System.currentTimeMillis(), 5,500,100,6,0);
    buffer.put(s1);
    
    Summary m1 = buffer.getMovingAggregate(0);
    Assert.assertEquals(5, m1.getCount());
    
    StatsSum s2 = new StatsSum(startTime+ONE_MIN, 10,1500,150,10,0);
    buffer.put(s2);
    
    Summary m2 = buffer.getMovingAggregate(0);
    Assert.assertEquals(15, m2.getCount());
    
    StatsSum s3 = new StatsSum(startTime+2*ONE_MIN, 20,1500,150,10,0);
    buffer.put(s3);
    
    Summary m3 = buffer.getMovingAggregate(0);
    Assert.assertEquals(35, m3.getCount());
    
    StatsSum s4 = new StatsSum(startTime+3*ONE_MIN, 1,100,100,100,0);
    buffer.put(s4);
    
    Summary m4 = buffer.getMovingAggregate(0);
    Assert.assertEquals(31, m4.getCount());
    
    StatsSum s5 = new StatsSum(startTime+4*ONE_MIN, 0,0,0,0,0);
    buffer.put(s5);
    
    Summary m5 = buffer.getMovingAggregate(0);
    Assert.assertEquals(21, m5.getCount());
    
    long sinceSeconds = m5.getSinceSeconds();
    Assert.assertTrue(sinceSeconds > 4*60);
  }
  
}
