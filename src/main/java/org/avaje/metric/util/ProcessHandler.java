package org.avaje.metric.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Handle the external process response (exit code, std out, std err).
 */
public class ProcessHandler {

  private static final Logger log = LoggerFactory.getLogger(ProcessHandler.class);

  private final Process process;

  private ProcessHandler(Process process) {
    this.process = process;
  }

  /**
   * Process a basic command.
   */
  public static ProcessResult command(String... command) {
    return process(new ProcessBuilder(command));
  }

  /**
   * Process a command.
   */
  public static ProcessResult process(ProcessBuilder pb) {
    try {
      ProcessResult result = process(pb.start());
      if (!result.success()) {
        throw new CommandException("command failed", result);
      }
      return result;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static ProcessResult process(Process process) {
    return new ProcessHandler(process).read();
  }

  private ProcessResult read() {

    try {
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

      List<String> stdOutLines = new ArrayList<>();
      List<String> stdErrLines = new ArrayList<>();

      String s;
      while ((s = stdError.readLine()) != null) {
        stdErrLines.add(s);
      }
      while ((s = stdInput.readLine()) != null) {
        stdOutLines.add(s);
      }

      int result = process.waitFor();
      ProcessResult pr = new ProcessResult(result, stdOutLines, stdErrLines);
      if (!pr.success() && log.isTraceEnabled()) {
        log.trace(pr.debug());
      }
      return pr;

    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
