# metrics-graphite

Sends metrics to Graphite using TCP protocol.

### Example use


```java

    GraphiteSender sender = GraphiteSender.builder()
      .hostname("grafana.foo.com")
      .port(4434)
      .batchSize(100)
      .prefix("dev.my-app.")
      .build();

    long epochSecs = System.currentTimeMillis() / 1000;
    sender.connect();
    sender.send("10", epochSecs, "test-metric", ".count");
    sender.send("300", epochSecs, "test-metric", ".total");
    sender.send("30", epochSecs, "test-metric", ".mean");
    sender.flush();

    sender.close();

```
