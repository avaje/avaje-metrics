package org.avaje.metric.util;

import java.net.URL;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

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
    
    
    Properties props = PropertiesLoader.indirectLoad("mylocations.txt");
    
    Assert.assertEquals("second file overrides first", "localOverride", props.getProperty("myappname"));
    Assert.assertEquals("loaded in second file", "localextra", props.getProperty("mylocalextra"));
    Assert.assertEquals("loaded in first file", "local", props.getProperty("mylocal"));
  }
}
