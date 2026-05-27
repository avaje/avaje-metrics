package io.avaje.metrics.graphite;

import io.avaje.metrics.Metric;
import io.avaje.metrics.Tags;
import io.avaje.metrics.stats.CounterStats;
import io.avaje.metrics.stats.GaugeDoubleStats;
import io.avaje.metrics.stats.GaugeLongStats;
import io.avaje.metrics.stats.TimerStats;
import org.junit.jupiter.api.Test;

import javax.net.SocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class DGraphiteSenderTest {

  @Test
  void sendTimer_withLabelTag_flattensLabelIntoMetricName() throws IOException {
    var socketFactory = new RecordingSocketFactory();
    var sender = sender(socketFactory);
    sender.connect();

    sender.send(List.of(new TimerStats(
      Metric.ID.of("web.api", Tags.of("env:prod", "label:MyController.myMethod")),
      2,
      40,
      30)));
    sender.flush();

    var payload = payload(socketFactory);
    assertThat(payload)
      .contains("'web.api.MyController.myMethod.count'")
      .contains("'web.api.MyController.myMethod.total'")
      .contains("'web.api.MyController.myMethod.mean'")
      .contains("'web.api.MyController.myMethod.max'")
      .doesNotContain("'web.api.count'");
  }

  @Test
  void sendTimer_withAppComponentLabelTag_usesLegacyAppMetricName() throws IOException {
    var socketFactory = new RecordingSocketFactory();
    var sender = sender(socketFactory);
    sender.connect();

    sender.send(List.of(new TimerStats(
      Metric.ID.of("app.component", Tags.of("label:MyClass.myMethod")),
      2,
      40,
      30)));
    sender.flush();

    var payload = payload(socketFactory);
    assertThat(payload)
      .contains("'app.MyClass.myMethod.count'")
      .contains("'app.MyClass.myMethod.total'")
      .contains("'app.MyClass.myMethod.mean'")
      .contains("'app.MyClass.myMethod.max'")
      .doesNotContain("'app.component.MyClass.myMethod.count'");
  }

  @Test
  void sendTimer_withoutLabelTag_keepsMetricName() throws IOException {
    var socketFactory = new RecordingSocketFactory();
    var sender = sender(socketFactory);
    sender.connect();

    sender.send(List.of(new TimerStats(Metric.ID.of("web.api", Tags.of("env:prod")), 2, 40, 30)));
    sender.flush();

    var payload = payload(socketFactory);
    assertThat(payload)
      .contains("'web.api.count'")
      .contains("'web.api.total'")
      .contains("'web.api.mean'")
      .contains("'web.api.max'")
      .doesNotContain("env:prod");
  }

  @Test
  void sendCounterAndGauges_withLabelTag_flattenLabelIntoMetricName() throws IOException {
    var socketFactory = new RecordingSocketFactory();
    var sender = sender(socketFactory);
    sender.connect();

    sender.send(List.of(
      new CounterStats(Metric.ID.of("custom.component", Tags.of("label:Service.count")), 42),
      new GaugeDoubleStats(Metric.ID.of("custom.component", Tags.of("label:Service.doubleGauge")), 12.5),
      new GaugeLongStats(Metric.ID.of("custom.component", Tags.of("label:Service.longGauge")), 99)));
    sender.flush();

    var payload = payload(socketFactory);
    assertThat(payload)
      .contains("'custom.component.Service.count'")
      .contains("'custom.component.Service.doubleGauge'")
      .contains("'custom.component.Service.longGauge'");
  }

  private DGraphiteSender sender(RecordingSocketFactory socketFactory) {
    var address = new InetSocketAddress(InetAddress.getLoopbackAddress(), 2003);
    return new DGraphiteSender(address, socketFactory, 500, null, 0);
  }

  private String payload(RecordingSocketFactory socketFactory) {
    var bytes = socketFactory.bytes();
    var payloadLength = ByteBuffer.wrap(bytes, 0, 4).getInt();
    assertThat(payloadLength).isEqualTo(bytes.length - 4);
    return new String(bytes, 4, payloadLength, UTF_8);
  }

  private static final class RecordingSocketFactory extends SocketFactory {

    private final RecordingSocket socket = new RecordingSocket();

    @Override
    public Socket createSocket() {
      return socket;
    }

    @Override
    public Socket createSocket(String host, int port) {
      return socket;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) {
      return socket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) {
      return socket;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) {
      return socket;
    }

    private byte[] bytes() {
      return socket.bytes();
    }
  }

  private static final class RecordingSocket extends Socket {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private boolean closed;

    @Override
    public OutputStream getOutputStream() {
      return output;
    }

    @Override
    public boolean isConnected() {
      return true;
    }

    @Override
    public boolean isClosed() {
      return closed;
    }

    @Override
    public synchronized void close() throws IOException {
      closed = true;
      super.close();
    }

    private byte[] bytes() {
      return output.toByteArray();
    }
  }
}
