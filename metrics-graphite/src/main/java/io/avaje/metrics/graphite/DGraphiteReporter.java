package io.avaje.metrics.graphite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


final class DGraphiteReporter implements GraphiteReporter {

  private static final Logger log = LoggerFactory.getLogger(GraphiteReporter.class);

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
      log.error("Error reporting metrics", e);
    } finally {
      try {
        sender.close();
      } catch (Throwable e) {
        log.error("Error closing graphite sender", e);
      }
      log.debug("metrics reported in {}ms", System.currentTimeMillis() - start);
    }
  }
}
