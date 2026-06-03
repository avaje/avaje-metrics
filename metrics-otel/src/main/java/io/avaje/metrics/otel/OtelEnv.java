package io.avaje.metrics.otel;

import java.time.Duration;

/**
 * Fallback resolution of OpenTelemetry SDK standard environment variables and
 * system properties used when builder fields are not explicitly set.
 *
 * <p>Resolution precedence (first non-blank wins):
 * <ol>
 *   <li>System property (e.g. {@code otel.exporter.otlp.endpoint})</li>
 *   <li>Environment variable (e.g. {@code OTEL_EXPORTER_OTLP_ENDPOINT})</li>
 * </ol>
 *
 * <p>For duration-valued settings, values are interpreted as milliseconds per the
 * OTel specification.
 */
final class OtelEnv {

  static final String OTLP_PROTOCOL_PROP = "otel.exporter.otlp.protocol";
  static final String OTLP_PROTOCOL_ENV = "OTEL_EXPORTER_OTLP_PROTOCOL";

  static final String OTLP_ENDPOINT_PROP = "otel.exporter.otlp.endpoint";
  static final String OTLP_ENDPOINT_ENV = "OTEL_EXPORTER_OTLP_ENDPOINT";

  static final String OTLP_TIMEOUT_PROP = "otel.exporter.otlp.timeout";
  static final String OTLP_TIMEOUT_ENV = "OTEL_EXPORTER_OTLP_TIMEOUT";

  static final String METRIC_EXPORT_INTERVAL_PROP = "otel.metric.export.interval";
  static final String METRIC_EXPORT_INTERVAL_ENV = "OTEL_METRIC_EXPORT_INTERVAL";

  static final String BSP_SCHEDULE_DELAY_PROP = "otel.bsp.schedule.delay";
  static final String BSP_SCHEDULE_DELAY_ENV = "OTEL_BSP_SCHEDULE_DELAY";

  static final String DEPLOYMENT_ENVIRONMENT_NAME_PROP = "otel.deployment.environment.name";
  static final String DEPLOYMENT_ENVIRONMENT_NAME_ENV = "OTEL_DEPLOYMENT_ENVIRONMENT_NAME";

  private OtelEnv() {
  }

  static String otlpEndpoint() {
    return readString(OTLP_ENDPOINT_PROP, OTLP_ENDPOINT_ENV);
  }

  /**
   * Read the configured OTLP exporter protocol from system property or environment
   * variable, returning {@code null} if not configured.
   *
   * <p>Recognised values per the OpenTelemetry SDK specification: {@code grpc} and
   * {@code http/protobuf}. The value {@code http/json} is rejected as it is not
   * supported by this module.
   */
  static MetricsOpenTelemetry.Protocol otlpProtocol() {
    var raw = readString(OTLP_PROTOCOL_PROP, OTLP_PROTOCOL_ENV);
    if (raw == null) {
      return null;
    }
    var lower = raw.toLowerCase();
    if (lower.equals("grpc")) {
      return MetricsOpenTelemetry.Protocol.GRPC;
    }
    if (lower.equals("http/protobuf")) {
      return MetricsOpenTelemetry.Protocol.HTTP_PROTOBUF;
    }
    throw new IllegalStateException(
      "Unsupported OTLP protocol '" + raw + "' from " + OTLP_PROTOCOL_PROP + " / "
        + OTLP_PROTOCOL_ENV + " — supported values: grpc, http/protobuf");
  }

  static Duration otlpTimeout() {
    return readDurationMillis(OTLP_TIMEOUT_PROP, OTLP_TIMEOUT_ENV);
  }

  static Duration metricExportInterval() {
    return readDurationMillis(METRIC_EXPORT_INTERVAL_PROP, METRIC_EXPORT_INTERVAL_ENV);
  }

  static Duration bspScheduleDelay() {
    return readDurationMillis(BSP_SCHEDULE_DELAY_PROP, BSP_SCHEDULE_DELAY_ENV);
  }

  static String deploymentEnvironmentName() {
    return readString(DEPLOYMENT_ENVIRONMENT_NAME_PROP, DEPLOYMENT_ENVIRONMENT_NAME_ENV);
  }

  private static String readString(String property, String env) {
    var value = System.getProperty(property);
    if (hasText(value)) {
      return value.trim();
    }
    value = System.getenv(env);
    if (hasText(value)) {
      return value.trim();
    }
    return null;
  }

  private static Duration readDurationMillis(String property, String env) {
    var raw = readString(property, env);
    if (raw == null) {
      return null;
    }
    try {
      var millis = Long.parseLong(raw);
      if (millis <= 0) {
        return null;
      }
      return Duration.ofMillis(millis);
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Invalid duration in milliseconds for " + property + " / " + env + ": '" + raw + "'", e);
    }
  }

  private static boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }
}
