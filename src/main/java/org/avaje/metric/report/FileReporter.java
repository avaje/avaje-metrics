package org.avaje.metric.report;

import java.io.File;
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
public class FileReporter extends BaseFileReporter implements MetricReporter {

  /**
   * The format that is written to the file.
   */
  protected final ReportWriter reportWriter;
  
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

    super(baseDirectory, baseFileName, numberOfFilesToKeep);
    this.reportWriter = (reportWriter != null) ? reportWriter : new CsvReportWriter(0);

    cleanup();
  }

  /**
   * Write the collected metrics to a file.
   */
  @Override
  public void report(ReportMetrics reportMetrics) {

    if (!enabled) {
      logger.debug("Not writing any metrics - disabled");
      return;
    }
    
    // Determine the file we should be writing to 
    String name = getFileName(baseFileName, new Date());
    File file = new File(new File(baseDirectory), name);
    FileOutput fileOutput = new FileOutput(file);

    try {
      reportWriter.write(fileOutput.getWriter(), reportMetrics);
    } catch (Exception e) {
      logger.error("Error trying to write metrics to file", e);
      
    } finally {
      fileOutput.close();
    }
  }


}
