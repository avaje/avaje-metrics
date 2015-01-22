package org.avaje.metric.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility that loads properties files.
 */
public final class PropertiesLoader {

  private static final Logger logger = Logger.getLogger(PropertiesLoader.class.getName());

  /**
   * Reads the file resources at the resourceLocation and reads these files to find where the
   * application specific properties files are located.
   */
  public static Properties indirectLoad(String resourceLocation) {
    return new PropertiesLoader().loadIndirectly(resourceLocation);
  }

  private final ClassLoader classLoader;

  /**
   * Construct with an explicit ClassLoader that is used to search for resources in the classPath.
   */
  public PropertiesLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  /**
   * Construct with the local ClassLoader.
   */
  public PropertiesLoader() {
    this.classLoader = getClass().getClassLoader();
  }

  /**
   * Loads properties by first searching for a file in the classPath at the provided
   * resourceLocation. This file contains a list of locations from which the properties should be
   * loaded from.
   * 
   * @param resourceLocation
   *          Location in the classPath of a file resource that has the locations of the properties
   *          files that should be loaded. Each line in the file resource is a location of a
   *          properties file.
   */
  public Properties loadIndirectly(String resourceLocation) {

    Properties properties = new Properties();
    List<String> locations = getLocations(resourceLocation);
    for (String location : locations) {
      load(properties, location);
    }

    return properties;
  }

  /**
   * Load and return the properties from the location.
   * <p>
   * The location can include a "classpath:" or "file:" prefix to explicitly indicate how to locate
   * the properties file. If neither prefix is used then both classPath and filePath locations are
   * searched for the properties file.
   * <p>
   * The location can include variables like ${user.home}. Both environment variables and system
   * properties are used to evaluate any placeholder expressions like the one above.
   * 
   * @param location
   *          The location of the properties file
   */
  public Properties load(String location) {

    Properties properties = new Properties();
    load(properties, location);
    return properties;
  }

  /**
   * Load properties from the file at the given location returning true if the properties file was
   * found and loaded.
   */
  public boolean load(Properties properties, String location) {

    // translate any ${environment variable} place holders
    String evalLocation = PropertyExpression.eval(location, null);

    boolean classPathLoad = false;
    boolean filePathLoad = false;

    if (evalLocation.startsWith("classpath:")) {
      classPathLoad = loadUsingClasspath(evalLocation, properties);

    } else if (evalLocation.startsWith("file:")) {
      filePathLoad = loadUsingFilePath(evalLocation, properties);

    } else {
      classPathLoad = loadUsingClasspath(evalLocation, properties);
      filePathLoad = loadUsingFilePath(evalLocation, properties);
    }

    if (classPathLoad) {
      logger.fine("loaded properties from classpath - " + evalLocation);
    }
    if (filePathLoad) {
      logger.fine("loaded properties from file system - " + evalLocation);
    }

    return classPathLoad || filePathLoad;
  }

  /**
   * Load the properties using a class path search for the properties file.
   */
  private boolean loadUsingClasspath(String evalLocation, Properties properties) {

    if (evalLocation.startsWith("classpath:")) {
      evalLocation = evalLocation.substring(10);
    }

    try {
      int count = 0;
      Enumeration<URL> resources = classLoader.getResources(evalLocation);

      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        try (InputStream inputStream = url.openStream()) {
          properties.load(inputStream);
          count++;
        } catch (IOException e) {
          logger.log(Level.SEVERE, "Error trying to read properties file " + evalLocation, e);
        }
      }

      return count > 0;

    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error trying to read properties file " + evalLocation, e);
      return false;
    }

  }

  /**
   * Load the properties using a file path search for the properties file.
   */
  private boolean loadUsingFilePath(String evalLocation, Properties properties) {

    if (evalLocation.startsWith("file:")) {
      evalLocation = evalLocation.substring(5);
    }

    File file = new File(evalLocation);
    if (!file.exists()) {
      return false;
    }
    try {
      properties.load(new FileReader(file));
      return true;

    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error trying to read properties file " + evalLocation, e);
      return false;
    }
  }

  /**
   * Read the classPath and filePath locations where the application properties files are read from.
   * These locations can include ${environment variable} placeholders like ${user.home} etc.
   */
  private List<String> getLocations(String resourceLocations) {

    List<String> locations = new ArrayList<>();

    try {

      Enumeration<URL> resources = classLoader.getResources(resourceLocations);

      // read all the matching resources but typically expecting one
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        try (InputStream inputStream = url.openStream()) {
          InputStreamReader reader = new InputStreamReader(inputStream);
          LineNumberReader lineReader = new LineNumberReader(reader);

          // read each line interpreting the line as a classPath or filePath
          // location to an application properties file
          String line = null;
          while ((line = lineReader.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0) {
              // add the non-empty line to the list of locations
              locations.add(line);
            }
          }
        }
      }

    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error trying to read resources from: " + resourceLocations, e);
    }

    return locations;
  }
}
