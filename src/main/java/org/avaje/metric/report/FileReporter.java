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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes the collected metrics to a file.
 * <p>
 * Typically you configure the frequency in seconds in which statistics are
 * collected and reported as well as a base directory where the metric files go.
 * By default the base directory will be read from a system property
 * 'metric.directory' and otherwise defaults to the current directory.
 * </p>
 */
public class FileReporter implements MetricReporter {

  private static final Logger logger = LoggerFactory.getLogger(FileReporter.class);

  protected static final int DEFAULT_NUM_FILES_TO_KEEP = 20;

  protected final int numberOfFilesToKeep;

  protected final String baseDirectory;

  protected final String baseFileName;

  protected final boolean enabled;

  /**
   * The format that is written to the file.
   */
  protected ReportWriter reportWriter;
  
  /**
   * Create with the defaults for base directory, file name, numberOfFilesToKeep and CsvReportWriter.
   */
  public FileReporter() {
    this(null);
  }

  /**
   * Create specifying a base directory where the metrics files should go.
   */
  public FileReporter(String baseDirectory) {
    this(baseDirectory, null);
  }

  /**
   * Create specifying a base directory and file name.
   */
  public FileReporter(String baseDirectory, String baseFileName) {
    this(baseDirectory, baseFileName, -1, null);
  }
  
  /**
   * Construct specifying a base directory, file name and report writer.
   */
  public FileReporter(String baseDirectory, String baseFileName, ReportWriter reportWriter) {
    this(baseDirectory, baseFileName, -1, reportWriter);
  }
  
  /**
   * Create specifying a write frequency, base directory and base file name.
   */
  public FileReporter(String baseDirectory, String baseFileName, int numberOfFilesToKeep, ReportWriter reportWriter) {

    this.numberOfFilesToKeep = getNumberOfFilesToKeep(numberOfFilesToKeep);
    this.baseDirectory = getBaseDirectory(baseDirectory);
    this.baseFileName = getBaseFileName(baseFileName);
    this.reportWriter = (reportWriter != null) ? reportWriter : new CsvReportWriter();

    cleanup();
    
    this.enabled = isWriteToFile();
    
    logger.debug("enabled:{} directory:{} name:{} numberOfFilesToKeep:{}", enabled, baseDirectory, baseFileName, numberOfFilesToKeep);
  }

  /**
   * Set the ReportWriter to use when writing the collected metrics.
   */
  public void setReportWriter(ReportWriter reportWriter) {
    this.reportWriter = reportWriter;
  }

  /**
   * Write the collected metrics to a file.
   */
  @Override
  public void report(ReportMetrics reportMetrics) {

    if (!enabled || reportWriter == null) {
      logger.debug("Not writing any metrics - disabled or reportWriter is not set");
      return;
    }
    
    // Determine the file we should be writing to 
    String name = getFileName(baseFileName, new Date());
    File file = new File(new File(baseDirectory), name);
    
    if (logger.isTraceEnabled()) {
      logger.trace("... write to file: {}", file.getAbsolutePath());
    }
    
    FileOutput fo = new FileOutput(file);
    try {
      Writer writer = fo.getWriter();
      reportWriter.write(writer, reportMetrics);

    } catch (Exception e) {
      logger.error("Error trying to write metrics to file", e);
      
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

      // determine the minimum file name based on todays date and numberOfFilesToKeep
      final String minFileName = getFileName(baseFileName, numberOfFilesToKeep);

      File dir = new File(baseDirectory);
      
      String[] delFileNames = dir.list(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return (name.startsWith(baseFileName) && name.compareTo(minFileName) < 0);
        }
      });

      
      logger.debug("cleaning up [{}] old metrics files", delFileNames.length);
      
      for (int i = 0; i < delFileNames.length; i++) {
        File f = new File(dir, delFileNames[i]);
        if (f.exists()) {
          if (!f.delete()) {
            logger.warn("Unable to delete old metric file: {}", f.getAbsoluteFile());
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
  class FileOutput {

    Writer writer;

    FileOutput(File f) {

      File parentFile = f.getParentFile();
      if (parentFile != null && !parentFile.exists()) {
        if (!parentFile.mkdirs()) {
          logger.warn("Was unable to make parent directories for file: "+f.getAbsolutePath());
        }
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
