package io.avaje.metrics.core;

import io.avaje.applog.AppLog;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

final class FileLines {

  private static final System.Logger log = AppLog.getLogger("io.avaje.metrics");

  private final File file;

  FileLines(String path) {
    file = new File(path);
  }

  boolean exists() {
    return file.exists();
  }

  long single() {
    final String val = readLine();
    return (val == null) ? 0 : Long.parseLong(val);
  }

  long singleMicros() {
    final String val = readLine();
    return (val == null) ? 0 : Long.parseLong(val) / 1000;
  }

  void readLines(Predicate<String> consumer) {
    try {
      try (LineNumberReader lineReader = new LineNumberReader(new FileReader(file))) {
        boolean readMore;
        do {
          String line = lineReader.readLine();
          readMore = line != null && consumer.test(line);
        } while (readMore);
      }
    } catch (IOException e) {
      log.log(Level.WARNING, "Error reading metrics file " + file, e);
    }
  }

  List<String> readLines() {
    try {
      List<String> lines = new ArrayList<>(5);
      try (LineNumberReader lineReader = new LineNumberReader(new FileReader(file))) {
        String line;
        while ((line = lineReader.readLine()) != null) {
          lines.add(line);
        }
        return lines;
      }

    } catch (IOException e) {
      log.log(Level.WARNING, "Error reading metrics file " + file, e);
      return Collections.emptyList();
    }
  }

  private @Nullable String readLine() {
    try {
      try (LineNumberReader lineReader = new LineNumberReader(new FileReader(file))) {
        return lineReader.readLine();
      }
    } catch (IOException e) {
      log.log(Level.WARNING, "Error reading metrics file " + file, e);
    }
    return null;
  }
}
