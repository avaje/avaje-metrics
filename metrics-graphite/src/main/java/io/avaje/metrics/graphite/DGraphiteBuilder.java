package io.avaje.metrics.graphite;

import io.avaje.metrics.MetricRegistry;
import io.avaje.metrics.MetricSupplier;
import io.avaje.metrics.Metrics;
import io.ebean.Database;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

final class DGraphiteBuilder implements GraphiteReporter.Builder {

  private int batchSize = 500;
  private String prefix;
  private String hostname;
  private int port;
  private SocketFactory socketFactory = SSLSocketFactory.getDefault();

  private final List<GraphiteSender.Reporter> reporters = new ArrayList<>();
  private boolean excludeDefaultRegistry;

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
  public DGraphiteBuilder excludeDefaultRegistry() {
    this.excludeDefaultRegistry = true;
    return this;
  }
    @Override
  public DGraphiteBuilder database(Database database) {
    reporters.add(DatabaseReporter.reporter(database));
    return this;
  }

  @Override
  public DGraphiteBuilder registry(MetricRegistry registry) {
    reporters.add(new DRegistryReporter(registry));
    return this;
  }

  @Override
  public DGraphiteBuilder registry(MetricSupplier supplier) {
    reporters.add(new DSupplierReporter(supplier));
    return this;
  }

  private GraphiteSender buildSender() {
    if (hostname == null) throw new IllegalStateException("hostname required");
    if (port == 0) throw new IllegalStateException("port must be set");

    InetSocketAddress address = new InetSocketAddress(hostname, port);
    if (address.getAddress() == null) {
      throw new IllegalStateException("Unknown host " + address.getHostName());
    }

    return new DGraphiteSender(address, socketFactory, batchSize, prefix);
  }

  public GraphiteReporter build() {
    if (!excludeDefaultRegistry){
      reporters.add(new DRegistryReporter(Metrics.registry()));
    }
    return new DGraphiteReporter(buildSender(), reporters);
  }

}
