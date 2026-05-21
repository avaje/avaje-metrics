package io.avaje.metrics.statsd;

import com.timgroup.statsd.StatsDClient;
import io.avaje.metrics.Metrics;
import io.avaje.metrics.Tags;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ReporterTest {

  @Test
  void visitTimer_withExistingLabelTag_keepsMetricNameAndSingleLabel() {
    var registry = Metrics.createRegistry();
    registry.timer("app.component", Tags.of("env:dev", "label:DefaultTimedResource.defaultMethod")).time(() -> {});

    var calls = new ArrayList<MetricCall>();
    var reporter = new Reporter(registry, recordingClient(calls), 0, 60, TimeUnit.SECONDS, List.of());

    reporter.run();

    assertThat(calls).hasSize(4);
    assertThat(calls).allSatisfy(call -> {
      assertThat(call.metricName()).startsWith("app.component.");
      assertThat(call.tags()).containsExactlyInAnyOrder("env:dev", "label:DefaultTimedResource.defaultMethod");
      assertThat(call.labelTags()).containsExactly("label:DefaultTimedResource.defaultMethod");
    });
  }

  @Test
  void visitTimer_withoutLabelTag_derivesAppComponentLabel() {
    var registry = Metrics.createRegistry();
    registry.timer("app.SimpleService.doSomething", Tags.of("env:dev")).time(() -> {});

    var calls = new ArrayList<MetricCall>();
    var reporter = new Reporter(registry, recordingClient(calls), 0, 60, TimeUnit.SECONDS, List.of());

    reporter.run();

    assertThat(calls).hasSize(4);
    assertThat(calls).allSatisfy(call -> {
      assertThat(call.metricName()).startsWith("app.component.");
      assertThat(call.tags()).containsExactlyInAnyOrder("env:dev", "label:SimpleService.doSomething");
      assertThat(call.labelTags()).containsExactly("label:SimpleService.doSomething");
    });
  }

  private StatsDClient recordingClient(List<MetricCall> calls) {
    return (StatsDClient) Proxy.newProxyInstance(
      StatsDClient.class.getClassLoader(),
      new Class<?>[]{StatsDClient.class},
      (proxy, method, args) -> {
        switch (method.getName()) {
          case "countWithTimestamp":
          case "gaugeWithTimestamp":
            calls.add(new MetricCall((String) args[0], (String[]) args[3]));
            return null;
          case "close":
          case "stop":
            return null;
          default:
            return null;
        }
      });
  }

  private static final class MetricCall {

    private final String metricName;
    private final String[] tags;

    private MetricCall(String metricName, String[] tags) {
      this.metricName = metricName;
      this.tags = tags;
    }

    private String metricName() {
      return metricName;
    }

    private String[] tags() {
      return tags;
    }

    private List<String> labelTags() {
      var labelTags = new ArrayList<String>();
      for (String tag : tags) {
        if (tag.startsWith("label:")) {
          labelTags.add(tag);
        }
      }
      return labelTags;
    }
  }
}
