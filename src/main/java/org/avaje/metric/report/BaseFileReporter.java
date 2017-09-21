package org.avaje.metric.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Writes the collected metrics to a file.
 * <p>
 * Typically you configure the frequency in seconds in which statistics are
 * collected and reported as well as a base directory where the metric files go.
 * By default the base directory will be read from a system property
 * 'metric.directory' and otherwise defaults to the current directory.
 * </p>
 */
public abstract class BaseFileReporter {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected static final int DEFAULT_NUM_FILES_TO_KEEP = 20;

  protected final int numberOfFilesToKeep;

  protected final String baseDirectory;

  protected final String baseFileName;

  protected final boolean enabled;

  /**
   * Create specifying a write frequency, base directory and base file name.
   */
  public BaseFileReporter(String baseDirectory, String baseFileName, int numberOfFilesToKeep) {

    this.numberOfFilesToKeep = getNumberOfFilesToKeep(numberOfFilesToKeep);
    this.baseDirectory = getBaseDirectory(baseDirectory);
    this.baseFileName = getBaseFileName(baseFileName);
    this.enabled = isWriteToFile();

    logger.debug("enabled:{} directory:{} name:{} numberOfFilesToKeep:{}", enabled, baseDirectory, baseFileName, numberOfFilesToKeep);
    if (enabled) {
      String name = getFileName(this.baseFileName, new Date());
      File file = new File(new File(this.baseDirectory), name);
      logger.debug("... write to file: {}", file.getAbsolutePath());
    }
  }


  /**
   * Cleanup old metric files.
   * <p>
   * This is done periodically to try and maintain numberOfFilesToKeep metric
   * files.
   * </p>
   */
  public void cleanup() {
    
    deleteOldMetricFiles();
  }

  /**
   * Delete old metric files.
   * <p>
   * This is called periodically to cleanup.
   * </p>
   */
  protected void deleteOldMetricFiles() {

    try {

      // determine the minimum file name based on today's date and numberOfFilesToKeep
      final String minFileName = getFileName(baseFileName, numberOfFilesToKeep);

      File dir = new File(baseDirectory);
      if (!dir.exists()) {
        // directory does not exist yet so no cleanup required
        return;
      }

      String[] delFileNames = dir.list((dir1, name) -> (name.startsWith(baseFileName) && name.compareTo(minFileName) < 0));
      logger.debug("cleaning up [{}] old metrics files", delFileNames.length);

      if (delFileNames != null) {
        for (String delFileName : delFileNames) {
          File f = new File(dir, delFileName);
          if (f.exists()) {
            if (!f.delete()) {
              logger.warn("Unable to delete old metric file: {}", f.getAbsoluteFile());
            }
          }
        }
      }

    } catch (Exception e) {
      logger.error("Error trying to cleanup old metric files", e);
    }
  }

  /**
   * Return true if the metric.writeToFile system property allows writing.
   */
  protected boolean isWriteToFile() {
    String value = System.getProperty("metric.writeToFile");
    return (value == null || !value.trim().toLowerCase().equals("false"));
  }

  /**
   * Return the number of files to keep in the directory.
   */
  protected static int getNumberOfFilesToKeep(int value) {

    if (value > 0) {
      return value;
    }

    String sysVal = System.getProperty("metric.numberOfFilesToKeep");
    if (sysVal != null) {
      try {
        return Integer.parseInt(sysVal);
      } catch (NumberFormatException e) {
        return DEFAULT_NUM_FILES_TO_KEEP;
      }
    }

    return DEFAULT_NUM_FILES_TO_KEEP;
  }

  /**
   * Return the base directory to put metrics files in - defaults to current
   * working directory.
   */
  protected String getBaseDirectory(String baseDirectory) {
    if (baseDirectory == null) {
      baseDirectory = System.getProperty("metric.directory");
    }
    if (baseDirectory == null) {
      baseDirectory = ".";
    }
    return baseDirectory;
  }

  /**
   * Return the base file name - defaults to "metric".
   */
  protected String getBaseFileName(String baseFileName) {
    if (baseFileName == null) {
      baseFileName = System.getProperty("metric.file");
    }
    if (baseFileName == null) {
      baseFileName = "metric";
    }
    return baseFileName.trim();
  }


  protected static String getFileName(String baseFileName, Date forDate) {
    String todayString = new SimpleDateFormat("yyyyMMdd").format(forDate);
    return baseFileName+ '-' + todayString + ".log";
  }

  public static String getFileName(String baseFileName, int daysAgo) {
    
    Calendar c = Calendar.getInstance();
    c.add(Calendar.DATE, daysAgo * -1);
    Date daysAgoDate = c.getTime();
    
    return getFileName(baseFileName, daysAgoDate);    
  }
  
  /**
   * A helper class for FileReporter that handles the file directory, name and
   * writer etc.
   */
  protected class FileOutput {

    private Writer writer;

    FileOutput(File file) {
      File parentFile = file.getParentFile();
      if (parentFile != null && !parentFile.exists()) {
        if (!parentFile.mkdirs()) {
          logger.warn("Was unable to make parent directories for file: " + file.getAbsolutePath());
        }
      }

      try {
        FileWriter fileWriter = new FileWriter(file, true);
        writer = new BufferedWriter(fileWriter, 2048);

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public Writer getWriter() {
      return writer;
    }
    
    public void close() {
      try {
        writer.flush();
      } catch (IOException e) {
        logger.error("Failed to flush metric file writer", e);
      }
      try {
        writer.close();
      } catch (IOException e) {
        logger.error("Failed to close metric file writer", e);
      }
    }

  }

}
