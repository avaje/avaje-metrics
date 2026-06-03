package io.avaje.metrics.otel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtelEnvTest {

  @AfterEach
  void tearDown() {
    System.clearProperty(OtelEnv.OTLP_ENDPOINT_PROP);
    System.clearProperty(OtelEnv.OTLP_PROTOCOL_PROP);
    System.clearProperty(OtelEnv.OTLP_TIMEOUT_PROP);
    System.clearProperty(OtelEnv.METRIC_EXPORT_INTERVAL_PROP);
    System.clearProperty(OtelEnv.BSP_SCHEDULE_DELAY_PROP);
    System.clearProperty(OtelEnv.DEPLOYMENT_ENVIRONMENT_NAME_PROP);
  }

  @Test
  void otlpEndpoint_default_null() {
    assertThat(OtelEnv.otlpEndpoint()).isNull();
  }

  @Test
  void otlpEndpoint_fromSystemProperty() {
    System.setProperty(OtelEnv.OTLP_ENDPOINT_PROP, "https://collector.example:4318");
    assertThat(OtelEnv.otlpEndpoint()).isEqualTo("https://collector.example:4318");
  }

  @Test
  void otlpEndpoint_blank_isNull() {
    System.setProperty(OtelEnv.OTLP_ENDPOINT_PROP, "   ");
    assertThat(OtelEnv.otlpEndpoint()).isNull();
  }

  @Test
  void otlpEndpoint_trimmed() {
    System.setProperty(OtelEnv.OTLP_ENDPOINT_PROP, "  https://x:4318  ");
    assertThat(OtelEnv.otlpEndpoint()).isEqualTo("https://x:4318");
  }

  @Test
  void otlpTimeout_default_null() {
    assertThat(OtelEnv.otlpTimeout()).isNull();
  }

  @Test
  void otlpTimeout_fromSystemProperty_millis() {
    System.setProperty(OtelEnv.OTLP_TIMEOUT_PROP, "15000");
    assertThat(OtelEnv.otlpTimeout()).isEqualTo(Duration.ofSeconds(15));
  }

  @Test
  void otlpTimeout_zeroOrNegative_null() {
    System.setProperty(OtelEnv.OTLP_TIMEOUT_PROP, "0");
    assertThat(OtelEnv.otlpTimeout()).isNull();
    System.setProperty(OtelEnv.OTLP_TIMEOUT_PROP, "-5");
    assertThat(OtelEnv.otlpTimeout()).isNull();
  }

  @Test
  void otlpTimeout_invalid_throws() {
    System.setProperty(OtelEnv.OTLP_TIMEOUT_PROP, "abc");
    assertThatThrownBy(OtelEnv::otlpTimeout)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining(OtelEnv.OTLP_TIMEOUT_PROP);
  }

  @Test
  void metricExportInterval_fromSystemProperty() {
    System.setProperty(OtelEnv.METRIC_EXPORT_INTERVAL_PROP, "120000");
    assertThat(OtelEnv.metricExportInterval()).isEqualTo(Duration.ofMinutes(2));
  }

  @Test
  void bspScheduleDelay_fromSystemProperty() {
    System.setProperty(OtelEnv.BSP_SCHEDULE_DELAY_PROP, "2500");
    assertThat(OtelEnv.bspScheduleDelay()).isEqualTo(Duration.ofMillis(2500));
  }

  @Test
  void deploymentEnvironmentName_fromSystemProperty() {
    System.setProperty(OtelEnv.DEPLOYMENT_ENVIRONMENT_NAME_PROP, "test");
    assertThat(OtelEnv.deploymentEnvironmentName()).isEqualTo("test");
  }

  @Test
  void otlpProtocol_default_null() {
    assertThat(OtelEnv.otlpProtocol()).isNull();
  }

  @Test
  void otlpProtocol_grpc() {
    System.setProperty(OtelEnv.OTLP_PROTOCOL_PROP, "grpc");
    assertThat(OtelEnv.otlpProtocol()).isEqualTo(MetricsOpenTelemetry.Protocol.GRPC);
  }

  @Test
  void otlpProtocol_httpProtobuf_caseInsensitive() {
    System.setProperty(OtelEnv.OTLP_PROTOCOL_PROP, "HTTP/Protobuf");
    assertThat(OtelEnv.otlpProtocol()).isEqualTo(MetricsOpenTelemetry.Protocol.HTTP_PROTOBUF);
  }

  @Test
  void otlpProtocol_unsupported_throws() {
    System.setProperty(OtelEnv.OTLP_PROTOCOL_PROP, "http/json");
    assertThatThrownBy(OtelEnv::otlpProtocol)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("http/json");
  }
}
