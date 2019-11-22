package io.avaje.metrics.report;

import io.avaje.metrics.RequestTiming;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Writes the collected request timings to a file.
 */
public class RequestFileReporter extends BaseFileReporter implements RequestTimingReporter {

  /**
   * The format that is written to the file.
   */
  protected final RequestTimingWriter reportWriter;

  /**
   * Create specifying a base directory and file name.
   */
  public RequestFileReporter(String baseDirectory, String baseFileName) {
    this(baseDirectory, baseFileName, -1, 0, null);
  }

  /**
   * Construct specifying a base directory, file name and report writer.
   */
  public RequestFileReporter(MetricReportConfig config) {
    this(config.getDirectory(), config.getRequestsFileName(), -1, config.getRequestTimingThreshold(), null);
  }

  /**
   * Create specifying a base directory, base file name, number of files to keep and reportWriter.
   */
  public RequestFileReporter(String baseDirectory, String baseFileName, int numberOfFilesToKeep, int thresholdPercentage, RequestTimingWriter reportWriter) {

    super(baseDirectory, baseFileName, numberOfFilesToKeep);
    this.reportWriter = (reportWriter != null) ? reportWriter : new BasicRequestTimingWriter(thresholdPercentage);

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
