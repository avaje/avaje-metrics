package io.avaje.metrics.otel;

import io.opentelemetry.sdk.trace.samplers.Sampler;

import static java.util.Objects.requireNonNull;

final class TraceSampling {

  static final String TRACES_SAMPLER_PROPERTY = "otel.traces.sampler";
  static final String TRACES_SAMPLER_ENV = "OTEL_TRACES_SAMPLER";
  static final String TRACES_SAMPLER_ARG_PROPERTY = "otel.traces.sampler.arg";
  static final String TRACES_SAMPLER_ARG_ENV = "OTEL_TRACES_SAMPLER_ARG";

  private TraceSampling() {
  }

  static Sampler parentBasedTraceIdRatio(double ratio) {
    validateRatio(ratio);
    return Sampler.parentBased(Sampler.traceIdRatioBased(ratio));
  }

  static void validateRatio(double ratio) {
    if (!Double.isFinite(ratio) || ratio < 0 || ratio > 1) {
      throw new IllegalArgumentException("Trace sample ratio must be between 0.0 and 1.0 inclusive");
    }
  }

  static Sampler configuredSampler() {
    var samplerName = configuredValue(TRACES_SAMPLER_PROPERTY, TRACES_SAMPLER_ENV);
    if (!hasText(samplerName)) {
      return null;
    }
    return sampler(samplerName.trim(), configuredValue(TRACES_SAMPLER_ARG_PROPERTY, TRACES_SAMPLER_ARG_ENV));
  }

  private static Sampler sampler(String rawName, String rawArg) {
    var name = requireNonNull(rawName, "sampler").trim().toLowerCase();
    switch (name) {
      case "always_on":
        return Sampler.alwaysOn();
      case "always_off":
        return Sampler.alwaysOff();
      case "traceidratio":
        return Sampler.traceIdRatioBased(ratioArg(name, rawArg));
      case "parentbased_always_on":
        return Sampler.parentBased(Sampler.alwaysOn());
      case "parentbased_always_off":
        return Sampler.parentBased(Sampler.alwaysOff());
      case "parentbased_traceidratio":
        return parentBasedTraceIdRatio(ratioArg(name, rawArg));
      default:
        throw new IllegalArgumentException("Unsupported trace sampler '" + rawName + "'");
    }
  }

  private static double ratioArg(String samplerName, String rawArg) {
    if (!hasText(rawArg)) {
      throw new IllegalArgumentException("Trace sampler '" + samplerName + "' requires " + TRACES_SAMPLER_ARG_PROPERTY);
    }
    try {
      var ratio = Double.parseDouble(rawArg.trim());
      validateRatio(ratio);
      return ratio;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid trace sample ratio '" + rawArg + "'", e);
    }
  }

  private static String configuredValue(String propertyName, String envName) {
    var property = System.getProperty(propertyName);
    if (hasText(property)) {
      return property;
    }
    var env = System.getenv(envName);
    return hasText(env) ? env : null;
  }

  private static boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }
}
