package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.junit.Assert;
import org.junit.Test;

public class DefaultMetricNameTest {
  
  @Test
  public void testParse() {
    
    MetricName name = DefaultMetricName.parse("org.test.Hello.rob");
    
    Assert.assertNotNull(name);
    Assert.assertEquals("org.test",name.getGroup());
    Assert.assertEquals("Hello",name.getType());
    Assert.assertEquals("rob",name.getName());
    
    MetricName name2 = DefaultMetricName.parse("test.Hello.rob");
    
    Assert.assertNotNull(name);
    Assert.assertEquals("test",name2.getGroup());
    Assert.assertEquals("Hello",name2.getType());
    Assert.assertEquals("rob",name2.getName());
    
    MetricName name3 = DefaultMetricName.parse("Hello.rob");
    
    Assert.assertNotNull(name);
    Assert.assertEquals("o",name3.getGroup());
    Assert.assertEquals("Hello",name3.getType());
    Assert.assertEquals("rob",name3.getName());
  }
}
