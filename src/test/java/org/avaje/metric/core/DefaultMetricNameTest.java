package org.avaje.metric.core;

import org.avaje.metric.MetricName;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class DefaultMetricNameTest {
  
  @Test
  public void testParse() {
    
    MetricName name = DefaultMetricName.parse("org.test.Hello.rob");
    
    assertNotNull(name);
    assertEquals("org.test",name.getGroup());
    assertEquals("Hello",name.getType());
    assertEquals("rob",name.getName());
    
    MetricName name2 = DefaultMetricName.parse("test.Hello.rob");
    
    assertNotNull(name);
    assertEquals("test",name2.getGroup());
    assertEquals("Hello",name2.getType());
    assertEquals("rob",name2.getName());
    
    MetricName name3 = DefaultMetricName.parse("Hello.rob");
    
    assertNotNull(name);
    assertEquals("o",name3.getGroup());
    assertEquals("Hello",name3.getType());
    assertEquals("rob",name3.getName());
  }
}
