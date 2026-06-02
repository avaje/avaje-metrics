package io.avaje.metrics.otel;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

final class ResourceAttributes {

  static final String RESOURCE_ATTRIBUTES_PROPERTY = "otel.resource.attributes";
  static final String RESOURCE_ATTRIBUTES_ENV = "OTEL_RESOURCE_ATTRIBUTES";
  static final String SERVICE_NAME_PROPERTY = "otel.service.name";
  static final String SERVICE_NAME_ENV = "OTEL_SERVICE_NAME";
  static final String DEPLOYMENT_ENVIRONMENT_NAME = "deployment.environment.name";
  static final String SERVICE_NAMESPACE = "service.namespace";

  private ResourceAttributes() {
  }

  static Map<String, String> configuredAttributes() {
    var property = System.getProperty(RESOURCE_ATTRIBUTES_PROPERTY);
    if (property != null) {
      return parse(property);
    }
    var env = System.getenv(RESOURCE_ATTRIBUTES_ENV);
    if (env != null) {
      return parse(env);
    }
    return Map.of();
  }

  static String configuredServiceName() {
    var property = System.getProperty(SERVICE_NAME_PROPERTY);
    if (hasText(property)) {
      return property.trim();
    }
    var env = System.getenv(SERVICE_NAME_ENV);
    if (hasText(env)) {
      return env.trim();
    }
    return null;
  }

  static Map<String, String> parse(String source) {
    requireNonNull(source, "source");
    var attributes = new LinkedHashMap<String, String>();
    for (String rawEntry : source.split(",", -1)) {
      var entry = rawEntry.trim();
      if (entry.isEmpty()) {
        continue;
      }
      var equals = entry.indexOf('=');
      if (equals < 0) {
        throw new IllegalArgumentException(
          "Invalid resource attribute entry '" + entry + "' expected key=value");
      }
      var key = entry.substring(0, equals).trim();
      if (key.isEmpty()) {
        throw new IllegalArgumentException(
          "Invalid resource attribute entry '" + entry + "' has blank key");
      }
      attributes.put(key, decode(entry.substring(equals + 1).trim()));
    }
    return attributes;
  }

  static void put(Map<String, String> attributes, String key, String value) {
    requireNonNull(attributes, "attributes");
    requireNonNull(key, "key");
    requireNonNull(value, "value");
    var trimmedKey = key.trim();
    if (trimmedKey.isEmpty()) {
      throw new IllegalArgumentException("Resource attribute key must not be blank");
    }
    attributes.put(trimmedKey, value);
  }

  private static boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }

  static Resource resource(Map<String, String> attributes) {
    AttributesBuilder builder = Attributes.builder();
    attributes.forEach(builder::put);
    return Resource.create(builder.build());
  }

  /**
   * Decode percent-encoded values using OpenTelemetry's W3C baggage-style behavior.
   */
  private static String decode(String value) {
    if (value.indexOf('%') < 0) {
      return value;
    }
    var bytes = new byte[value.length()];
    var pos = 0;
    for (var i = 0; i < value.length(); i++) {
      var c = value.charAt(i);
      if (c == '%' && i + 2 < value.length()) {
        var d1 = Character.digit(value.charAt(i + 1), 16);
        var d2 = Character.digit(value.charAt(i + 2), 16);
        if (d1 != -1 && d2 != -1) {
          bytes[pos++] = (byte) ((d1 << 4) + d2);
          i += 2;
          continue;
        }
      }
      bytes[pos++] = (byte) c;
    }
    return new String(bytes, 0, pos, StandardCharsets.UTF_8);
  }
}
