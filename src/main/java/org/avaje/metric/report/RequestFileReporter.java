package org.avaje.metric.report;

import org.avaje.metric.RequestTiming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Writes the collected request timings to a file.
 */
public class RequestFileReporter extends BaseFileReporter implements RequestTimingReporter {

  private static final Logger logger = LoggerFactory.getLogger(RequestFileReporter.class);

  /**
   * The format that is written to the file.
   */
  protected final RequestTimingWriter reportWriter;

  /**
   * Create with the defaults for base directory, file name, numberOfFilesToKeep and CsvReportWriter.
   */
  public RequestFileReporter() {
    this(null);
  }

  /**
   * Create specifying a base directory where the metrics files should go.
   */
  public RequestFileReporter(String baseDirectory) {
    this(baseDirectory, null);
  }

  /**
   * Create specifying a base directory and file name.
   */
  public RequestFileReporter(String baseDirectory, String baseFileName) {
    this(baseDirectory, baseFileName, -1, null);
  }

  /**
   * Construct specifying a base directory, file name and report writer.
   */
  public RequestFileReporter(String baseDirectory, String baseFileName, RequestTimingWriter reportWriter) {
    this(baseDirectory, baseFileName, -1, reportWriter);
  }

  /**
   * Create specifying a base directory, base file name, number of files to keep and reportWriter.
   */
  public RequestFileReporter(String baseDirectory, String baseFileName, int numberOfFilesToKeep, RequestTimingWriter reportWriter) {

    super(baseDirectory, baseFileName, numberOfFilesToKeep);
    this.reportWriter = (reportWriter != null) ? reportWriter : new BasicRequestTimingWriter();

    cleanup();
  }

  /**
   * Return the base file name - defaults to "metricrequest".
   */
  @Override
  protected String getBaseFileName(String baseFileName) {
    if (baseFileName == null) {
      baseFileName = System.getProperty("metricrequest.file");
    }
    if (baseFileName == null) {
      baseFileName = "metricrequest";
    }
    return baseFileName.trim();
  }

  /**
   * Write the collected metrics to a file.
   */
  @Override
  public void report(List<RequestTiming> requestTimings) {

    if (!enabled) {
      logger.debug("Not writing any request timings - disabled");
      return;
    }
    
    // Determine the file we should be writing to 
    String name = getFileName(baseFileName, new Date());
    File file = new File(new File(baseDirectory), name);
    FileOutput fileOutput = new FileOutput(file);

    try {
      reportWriter.write(fileOutput.getWriter(), requestTimings);
    } catch (Exception e) {
      logger.error("Error trying to write metrics to file", e);
      
    } finally {
      fileOutput.close();
    }
  }


}
