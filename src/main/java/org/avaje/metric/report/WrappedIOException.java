package org.avaje.metric.report;

import java.io.IOException;

public class WrappedIOException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  
  private IOException cause;
  
  public WrappedIOException(IOException cause) {
    this.cause = cause;
  }

  public IOException getCause() {
    return cause;
  }    
}