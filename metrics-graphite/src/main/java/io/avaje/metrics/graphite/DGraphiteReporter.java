package io.avaje.metrics.graphite;

import io.avaje.applog.AppLog;

import java.io.IOException;
import java.util.List;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;

final class DGraphiteReporter implements GraphiteReporter {

  private static final System.Logger log = AppLog.getLogger(GraphiteReporter.class);

  private final GraphiteSender sender;
  private final List<GraphiteSender.Reporter> reporters;

  DGraphiteReporter(GraphiteSender sender, List<GraphiteSender.Reporter> reporters) {
    this.sender = sender;
    this.reporters = reporters;
  }

  @Override
  public void report() {
    long start = System.currentTimeMillis();
    try {
      sender.connect();
      for (GraphiteSender.Reporter reporter : reporters) {
        reporter.report(sender);
      }
      sender.flush();
    } catch (IOException e) {
      log.log(ERROR, "Error reporting metrics", e);
    } finally {
      try {
        sender.close();
      } catch (Throwable e) {
        log.log(ERROR, "Error closing graphite sender", e);
      }
      log.log(DEBUG, "metrics reported in {0}ms", System.currentTimeMillis() - start);
    }
  }
}
