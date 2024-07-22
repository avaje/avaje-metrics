package io.avaje.metrics.graphite;

import io.avaje.applog.AppLog;
import io.avaje.metrics.*;

import javax.net.SocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A client to a Carbon server that sends all metrics after they have been pickled in configurable sized batches
 */
final class DGraphiteSender implements GraphiteSender {

  private static final System.Logger log = AppLog.getLogger(GraphiteReporter.class);

  /**
   * Minimally necessary pickle opcodes.
   */
  private static final char
    MARK = '(',
    STOP = '.',
    LONG = 'L',
    STRING = 'S',
    APPEND = 'a',
    LIST = 'l',
    TUPLE = 't',
    QUOTE = '\'',
    LF = '\n';


  private final int batchSize;
  private final List<MetricTuple> metrics = new ArrayList<>();
  private final InetSocketAddress address;
  private final SocketFactory socketFactory;
  private final String prefix;
  private final long timedThreshold;
  private Socket socket;

  DGraphiteSender(InetSocketAddress address, SocketFactory socketFactory, int batchSize, String prefix, long timedThreshold) {
    this.address = address;
    this.socketFactory = socketFactory;
    this.batchSize = batchSize;
    this.prefix = prefix;
    this.timedThreshold = timedThreshold;
  }

  @Override
  public void connect() throws IOException {
    if (isConnected()) {
      throw new IllegalStateException("Already connected");
    }
    this.socket = socketFactory.createSocket(address.getAddress(), address.getPort());
  }

  @Override
  public boolean isConnected() {
    return socket != null && socket.isConnected() && !socket.isClosed();
  }

  @Override
  public void send(String value, long timestamp, String... names) throws IOException {
    metrics.add(new MetricTuple(timestamp, value, names));
    if (metrics.size() >= batchSize) {
      writeMetrics();
    }
  }

  @Override
  public void send(List<Metric.Statistics> metrics) {
    var visitor = new MetricsVisitor();
    for (Metric.Statistics metric : metrics) {
      metric.visit(visitor);
    }
  }

  private class MetricsVisitor implements Metric.Visitor {

    private final long epochSecs = System.currentTimeMillis() / 1000;

    @Override
    public void visit(Timer.Stats timed) {
      if (timedThreshold == 0 || timedThreshold < timed.total()) {
        sendValues(timed);
      }
    }

    @Override
    public void visit(Meter.Stats stats) {
      sendValues(stats);
    }

    private void sendValues(Meter.Stats stats) {
      try {
        send(String.valueOf(stats.count()), epochSecs, stats.name(), ".count");
        send(String.valueOf(stats.total()), epochSecs, stats.name(), ".total");
        send(String.valueOf(stats.mean()), epochSecs, stats.name(), ".mean");
        send(String.valueOf(stats.max()), epochSecs, stats.name(), ".max");
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    @Override
    public void visit(Counter.Stats counter) {
      try {
        send(String.valueOf(counter.count()), epochSecs, counter.name());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    @Override
    public void visit(GaugeDouble.Stats gauge) {
      try {
        send(String.valueOf(gauge.value()), epochSecs, gauge.name());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    @Override
    public void visit(GaugeLong.Stats gauge) {
      try {
        send(String.valueOf(gauge.value()), epochSecs, gauge.name());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  @Override
  public void flush() throws IOException {
    writeMetrics();
  }

  @Override
  public void close() {
    try {
      flush();
    } catch (IOException e) {
      log.log(INFO, "Exception flushing metrics", e);
    } finally {
      closeSocket();
    }
  }

  private void closeSocket() {
    if (socket != null) {
      try {
        socket.close();
      } catch (IOException e) {
        log.log(INFO, "Exception trying to close socket", e);
      }
      socket = null;
    }
  }

  private void writeMetrics() throws IOException {
    if (!metrics.isEmpty()) {
      try {
        final byte[] payload = pickleMetrics(metrics);
        final byte[] header = ByteBuffer.allocate(4).putInt(payload.length).array();
        send(header, payload);
      } finally {
        // if there was an error we might drop some metrics
        metrics.clear();
      }
    }
  }

  private void send(byte[] header, byte[] payload) throws IOException {
    try {
      sendPayload(header, payload);
    } catch (IOException e) {
      // perform a single retry with a new socket connection
      log.log(WARNING, "Retry sending metrics due to " + e);
      closeSocket();
      this.socket = socketFactory.createSocket(address.getAddress(), address.getPort());
      sendPayload(header, payload);
    }
  }

  private void sendPayload(byte[] header, byte[] payload) throws IOException {
    OutputStream outputStream = socket.getOutputStream();
    outputStream.write(header);
    outputStream.write(payload);
    outputStream.flush();
  }

  /**
   * See: <a href="http://readthedocs.org/docs/graphite/en/1.0/feeding-carbon.html">feeding-carbon</a>
   */
  byte[] pickleMetrics(List<MetricTuple> metrics) throws IOException {
    // Extremely rough estimate of 75 bytes per message
    ByteArrayOutputStream out = new ByteArrayOutputStream(metrics.size() * 75);
    Writer pickled = new OutputStreamWriter(out, UTF_8);

    pickled.append(MARK);
    pickled.append(LIST);

    for (MetricTuple tuple : metrics) {
      // start the outer tuple
      pickled.append(MARK);

      // the metric name is a string.
      pickled.append(STRING);
      pickled.append(QUOTE);
      if (prefix != null) {
        pickled.append(prefix);
      }
      for (String name : tuple.names) {
        pickled.append(name);
      }
      pickled.append(QUOTE);
      pickled.append(LF);

      // start the inner tuple
      pickled.append(MARK);

      // timestamp is a long
      pickled.append(LONG);
      pickled.append(Long.toString(tuple.timestamp));
      // the trailing L is to match python's repr(long(1234))
      pickled.append(LONG);
      pickled.append(LF);

      // and the value is a string.
      pickled.append(STRING);
      pickled.append(QUOTE);
      pickled.append(tuple.value);
      pickled.append(QUOTE);
      pickled.append(LF);

      pickled.append(TUPLE); // inner close
      pickled.append(TUPLE); // outer close
      pickled.append(APPEND);
    }
    pickled.append(STOP);
    pickled.flush();
    return out.toByteArray();
  }

  private static final Pattern WHITESPACE = Pattern.compile("\\s+");
  private static final String DASH = "-";

  /**
   * Trims the string and replaces all whitespace characters with the provided symbol
   */
  @Override
  public String sanitize(String string) {
    return WHITESPACE.matcher(string.trim()).replaceAll(DASH);
  }

  static final class MetricTuple {

    final long timestamp;
    final String value;
    final String[] names;

    MetricTuple(long timestamp, String value, String... names) {
      this.timestamp = timestamp;
      this.value = value;
      this.names = names;
    }
  }
}
