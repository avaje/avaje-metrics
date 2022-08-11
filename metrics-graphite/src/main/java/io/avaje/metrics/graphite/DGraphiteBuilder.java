package io.avaje.metrics.graphite;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;

final class DGraphiteBuilder implements GraphiteSender.Builder {

  private int batchSize = 100;
  private String prefix;
  private String hostname;
  private int port;
  private SocketFactory socketFactory = SSLSocketFactory.getDefault();

  @Override
  public DGraphiteBuilder prefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  @Override
  public DGraphiteBuilder hostname(String hostname) {
    this.hostname = hostname;
    return this;
  }

  @Override
  public DGraphiteBuilder port(int port) {
    this.port = port;
    return this;
  }

  @Override
  public DGraphiteBuilder socketFactory(SocketFactory socketFactory) {
    this.socketFactory = socketFactory;
    return this;
  }

  @Override
  public DGraphiteBuilder batchSize(int batchSize) {
    this.batchSize = batchSize;
    return this;
  }

  @Override
  public GraphiteSender build() {
    if (hostname == null) throw new IllegalStateException("hostname required");
    if (port == 0) throw new IllegalStateException("port must be set");

    InetSocketAddress address = new InetSocketAddress(hostname, port);
    if (address.getAddress() == null) {
      throw new IllegalStateException("Unknown host " + address.getHostName());
    }

    return new DGraphiteSender(address, socketFactory, batchSize, prefix);
  }
}
