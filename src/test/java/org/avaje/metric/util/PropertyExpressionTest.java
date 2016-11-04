package org.avaje.metric.util;


import org.testng.Assert;
import org.testng.annotations.Test;

public class PropertyExpressionTest {

  @Test
  public void test() {
    
    String userHome = System.getProperty("user.home");
       
    Assert.assertEquals("hello", PropertyExpression.eval("hello", null));
    Assert.assertEquals("", PropertyExpression.eval("", null));
    Assert.assertEquals("{user.home}", PropertyExpression.eval("{user.home}", null));
    
    Assert.assertEquals(userHome, PropertyExpression.eval("${user.home}", null));
    Assert.assertEquals(userHome+"/foo", PropertyExpression.eval("${user.home}/foo", null));
    Assert.assertEquals("bar"+userHome+"boo", PropertyExpression.eval("bar${user.home}boo", null));

    
  }
}
