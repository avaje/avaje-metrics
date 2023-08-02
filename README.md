# avaje-metric-core

Please read the main documentation at: http://avaje-metrics.github.io

## Maven dependency

```xml
    <dependency>
      <groupId>io.avaje</groupId>
      <artifactId>avaje-metrics</artifactId>
      <version>9.2</version>
    </dependency>
```


## License - Apache 2
Published under Apache Software License 2.0, see LICENSE

## MetricRegistry

A MetricRegistry holds the metrics like timers, gauges etc.

```java
MetricRegistry registry = Metrics.createRegistry();

// obtain timers, counters, gauges etc
Timer timer = registry.timer("my.timer");
Counter counter = registry.counter("my.count");
```

## Metrics & default registry

There is default MetricRegistry. Creating metrics via `Metrics` creates metrics attached to the default registry.

```java
// obtain the default MetricRegistry
MetricRegistry defaultRegistry = Metrics.registry();
```

```java
// timer registered with the default MetricRegistry
Timer timer = Metrics.timer("my.timer");
```

## Counter

A Counter holds a single long value that is incremented

```java
Counter counter = registry.counter("my.count");

counter.inc();
counter.inc(42);
```

## Meter

A Meter is used to represent events that have a value such as bytes sent, bytes received, lines read etc.

```java
Meter meter = registry.meter("my.meter");

meter.addEvent(42);
meter.addEvent(44);
meter.addEvent(46);
```

## Gauge

Gauges are used to measure something that supplies a value. Creating a Gauge takes either a LongSupplier or a DoubleSupplier.

Gauges are used for JVM Memory metrics and Garbage collection. Although application can obtain values from a Gauge typically
we register the gauge and the value is obtained when the metrics are reported.

```java
registry.gauge("my.gauge0", myLongSupplier );
registry.gauge("my.gauge1", myDoubleSupplier );
```

There are `incrementing` wrappers that can be applied to the LongSupplier of DoubleSupplier to use for the case
when the gauge only increments and we wish to report the change.

```java
// wrap via incrementing()
GaugeLong myIncrementing = GaugeLong.incrementing(myLongSupplier);

registry.gauge("my.gauge", myIncrementing);
```

## Timer

A Timer measures the time on an event in microseconds.
It provides count, total time in micros, mean time in micros, max time in micros.

Timers can be obtained and used via code or via `@Timed` enhancement. We can also
apply timers on "All non-private methods of Spring Beans"
or "All non-private methods of Avaje Inject Beans"

#### @Timed

Timers can be added by putting `@Timed` on a class. Then enhancement will add timers to
all non-private methods on that class. We use `@NotTimed` to not have a method timed.


#### Using Timer programmatically

Obtain a Timer from the `MetricRegistry` or via `Metrics.timer(...)` which uses the default registry.

```java
Timer metric = Metrics.timer("test.runnable");
```

#### Time Runnable

The Timer can take a runnable like:

```java
Timer timer = Metrics.timer("test.runnable");

// using runnable
timer.time( //* ... runnable */ );
```

#### Time using start nanos
```java
long startNanos = System.nanoTime();

// do something we want to time ...
timer.add(startNanos);
```
This is an efficient way to time events with no extra object allocation.

The time is the difference between startNanos and when that is added to
the timer.


#### Time using startEvent
```java
Event event = timer.startEvent();
// do something we want to time ...
event.end();
```

