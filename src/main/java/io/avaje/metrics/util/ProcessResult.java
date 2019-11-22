package io.avaje.metrics.util;

import java.util.List;

/**
 * The result of an external process call.
 */
public class ProcessResult {

  final int result;
  final List<String> stdOutLines;
  final List<String> stdErrLines;

  /**
   * Create with the result exit code and std out and err content.
   */
  public ProcessResult(int result, List<String> stdOutLines, List<String> stdErrLines) {
    this.result = result;
    this.stdOutLines = stdOutLines;
    this.stdErrLines = stdErrLines;
  }

  /**
   * Return true if exit result was 0.
   */
  public boolean success() {
    return result == 0;
  }

  /**
   * Return the exit result code.
   */
  public int getResult() {
    return result;
  }

  /**
   * Return the STD OUT lines.
   */
  public List<String> getStdOutLines() {
    return stdOutLines;
  }

  /**
   * Return the STD ERR lines.
   */
  public List<String> getStdErrLines() {
    return stdErrLines;
  }

  /**
   * Return all the STD OUT content.
   */
  public String stdOut() {
    return lines(stdOutLines);
  }

  /**
   * Return all the STD err content.
   */
  public String stdErr() {
    return lines(stdErrLines);
  }

  /**
   * Return debug output.
   */
  public String debug() {
    return "exit:" + result + "\n out:" + stdOut() + "\n err:" + stdErr();
  }

  private String lines(List<String> lines) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < lines.size(); i++) {
      if (i > 0) {
        sb.append("\n");
      }
      sb.append(lines.get(i));
    }
    return sb.toString();
  }


}
