package org.avaje.metric.util;

import java.net.URL;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PropertiesLoaderTest {

  @Test
  public void test() {
    
    ClassLoader classLoader = PropertiesLoaderTest.class.getClassLoader();
    URL resource = classLoader.getResource("hello.properties");    
    Assert.assertNotNull(resource);

    resource = classLoader.getResource("subdir/extra.dummy.properties");    
    Assert.assertNotNull(resource);

    PropertiesLoader loader = new PropertiesLoader();

    Properties properties = loader.load("classpath:doesnotexist/extra.dummy.properties");    
    Assert.assertTrue(properties.isEmpty());

    properties = loader.load("classpath:subdir/extra.dummy.properties");    
    Assert.assertFalse(properties.isEmpty());
  }

  /**
   * Enable with file:${user.home}/config/dummy.properties
   */
  @Test(enabled = false)
  public void testWithUserHome() {

    Properties props = PropertiesLoader.indirectLoad("mylocations.txt");

    Assert.assertEquals("localOverride", props.getProperty("myappname"));
    Assert.assertEquals("localextra", props.getProperty("mylocalextra"));
    Assert.assertEquals("local", props.getProperty("mylocal"));
  }
}
