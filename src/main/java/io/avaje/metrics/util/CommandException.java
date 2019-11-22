package io.avaje.metrics.util;

/**
 * Exception throw when executing an OS command.
 */
public class CommandException extends RuntimeException {

  private ProcessResult result;

  CommandException(String message, ProcessResult result) {
    super(message);
    this.result = result;
  }

  public ProcessResult getResult() {
    return result;
  }
}
