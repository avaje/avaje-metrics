package org.avaje.metric.filereport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A helper class for FileReporter that handles the file directory, name and
 * writer etc.
 */
public class FileOutput {

  private static final Logger logger = Logger.getLogger(FileOutput.class.getName());

  protected Writer writer;

  public FileOutput(String baseDirectory, String baseFileName) {

    File f = new File(getDirectory(baseDirectory), getFileName(baseFileName));
    File parentFile = f.getParentFile();
    if (parentFile != null && !parentFile.exists()) {
      parentFile.mkdirs();
    }
    try {
      FileWriter fileWriter = new FileWriter(f, true);
      writer = new BufferedWriter(fileWriter, 1024);

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

  protected String getFileName(String baseFileName) {
    return getFileName(baseFileName, new Date());
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
}
