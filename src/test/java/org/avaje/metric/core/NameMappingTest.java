package org.avaje.metric.core;


import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class NameMappingTest {

  @Test(enabled = false)
  public void testGetMappedName() throws Exception {

    NameMapping nameMapping = new NameMapping(NameMappingTest.class.getClassLoader());

    assertEquals("org.test.junk", nameMapping.getMappedName("org.test.junk"));

    assertEquals("junk", nameMapping.getMappedName("org.foo.junk"));

    assertEquals("na", nameMapping.getMappedName("orange.truck"));
    assertEquals("na.test", nameMapping.getMappedName("orange.truck.test"));
    assertEquals("na.test.junk", nameMapping.getMappedName("orange.truck.test.junk"));
  }
}