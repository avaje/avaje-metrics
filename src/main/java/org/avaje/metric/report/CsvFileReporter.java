package org.avaje.metric.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.avaje.metric.Metric;

/**
 * Writes the collected metrics to a file.
 * <p>
 * Typically you configure the frequency in seconds in which statistics are
 * collected and reported as well as a base directory where the metric files go.
 * By default the base directory will be read from a system property
 * 'metric.directory' and otherwise defaults to the current directory.
 * </p>
 */
public class CsvFileReporter implements MetricReporter {

  private static final Logger logger = Logger.getLogger(CsvFileReporter.class.getName());

  protected static final int DEFAULT_NUM_FILES_TO_KEEP = 20;

  protected final int numberOfFilesToKeep;

  protected final String baseDirectory;

  protected final String baseFileName;

  protected final boolean enabled;

  /**
   * Create with a freqInSeconds of 60 and using the default base directory.
   */
  public CsvFileReporter() {
    this(null);
  }

  /**
   * Create specifying a base directory where the metrics files should go.
   */
  public CsvFileReporter(String baseDirectory) {
    this(baseDirectory, null);
  }

  /**
   * Create specifying a write frequency, base directory and base file name.
   */
  public CsvFileReporter(String baseDirectory, String baseFileName) {
    this(baseDirectory, baseFileName, -1);
  }

  /**
   * Create specifying a write frequency, base directory and base file name.
   */
  public CsvFileReporter(String baseDirectory, String baseFileName, int numberOfFilesToKeep) {

    this.numberOfFilesToKeep = getNumberOfFilesToKeep(numberOfFilesToKeep);
    this.baseDirectory = getBaseDirectory(baseDirectory);
    this.baseFileName = getBaseFileName(baseFileName);

    cleanup();
    
    this.enabled = isWriteToFile();
  }

  @Override
  public void report(List<Metric> metrics) {

    if (!enabled) {
      // disabled via system property - metric.writeToFile
      return;
    }
    String name = getFileName(baseFileName, new Date());
    
    FileOutput fo = new FileOutput(baseDirectory, name);
    try {
      Writer writer = fo.getWriter();

      CsvWriteVisitor visitor = new CsvWriteVisitor(writer);
   
      for (Metric metric : metrics) {
        metric.visit(visitor);
      }

    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error trying to write metrics to file", e);
      
    } finally {
      fo.close();
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

      final String minFileName = getFileName(baseFileName, numberOfFilesToKeep);

      File dir = new File(baseDirectory);
      String[] delFileNames = dir.list(new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
          return (name.startsWith(baseFileName) && name.compareTo(minFileName) < 0);
        }
      });

      for (int i = 0; i < delFileNames.length; i++) {
        File f = new File(dir, delFileNames[i]);
        if (f.exists()) {
          f.delete();
        }
      }

    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error trying to cleanup old metric files", e);
    }
  }

  /**
   * Return true if the metric.writeToFile system property allows writing.
   */
  protected boolean isWriteToFile() {
    String value = System.getProperty("metric.writeToFile");
    return (value == null || !value.trim().toLowerCase().equals("false"));
  }

  protected static int getNumberOfFilesToKeep(int value) {
    if (value < 1) {
      String sysVal = System.getProperty("metric.numberOfFilesToKeep");
      if (sysVal != null) {
        try {
          return Integer.parseInt(sysVal);
        } catch (NumberFormatException e) {
          return DEFAULT_NUM_FILES_TO_KEEP;
        }
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
    return baseFileName;
  }


  protected static String getFileName(String baseFileName, Date forDate) {
    String todayString = new SimpleDateFormat("yyyyMMdd").format(forDate);
    return baseFileName+"-" + todayString + ".txt";
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
  public class FileOutput {

    protected Writer writer;

    public FileOutput(String baseDirectory, String baseFileName) {

      File f = new File(getDirectory(baseDirectory), baseFileName);
      File parentFile = f.getParentFile();
      if (parentFile != null && !parentFile.exists()) {
        parentFile.mkdirs();
      }
      try {
        FileWriter fileWriter = new FileWriter(f, true);
        writer = new BufferedWriter(fileWriter, 4096);

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
        logger.log(Level.SEVERE, "Failed to flush metric file writer", e);
      }
      try {
        writer.close();
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Failed to close metric file writer", e);
      }
    }

    protected File getDirectory(String baseDirectory) {
      return new File(baseDirectory);
    }

  }

}
