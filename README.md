# avaje-metric-core

Please read the main documentation at: http://avaje-metrics.github.io

## Maven dependency

```xml
    <dependency>
      <groupId>org.avaje.metric</groupId>
      <artifactId>avaje-metric-core</artifactId>
      <version>4.4.1</version>
    </dependency>
```


## License - Apache 2
Published under Apache Software License 2.0, see LICENSE

## Overhead
Micro benchmarks are notoriously difficult but that said the overhead using version 4.1 has been measured at 140 nanos per method invocation (with request collection count of 0 meaning per request timing isn't collected). For perspective if the overhead was 200 nanos then executing 5000 methods would add 1 millisecond of overhead.

Request timing would not add much more to execution time per say but relatively speaking can produce a lot of output (that is reported in a background thread) and creates objects so adds some extra GC cost. In production you would expect to limit the number of metrics you collect on and limit the number of request timings you collect.

## Example Metrics output (typically reported every 60 seconds)  
Below is a sample of the metric log output. The metrics are periodically collected 
and output to a file or sent to a repository.

```console
14:01:00, lg, jvm.gc.ps-marksweep, count=0, time=0
14:01:00, lg, jvm.gc.ps-scavenge, count=0, time=0
14:01:00, dg, jvm.memory.heap, init=250.2, used=64.59, committed=240.0, max=3559.0, pct=1.0
14:01:00, dg, jvm.memory.nonheap, init=23.44, used=20.67, committed=23.44, max=214.0, pct=9.0
14:01:00, dm, jvm.os.loadAverage, value=1.06
14:01:00, dm, jvm.system.uptime, value=5.0
14:01:00, lg, jvm.threads, current=17, peak=18, daemon=5
14:01:00, tm, org.example.myapp.service.DummyEmailSender.send, count=1, avg=140, max=140, sum=140, dur=10, err.count=0
14:01:00, tm, org.example.myapp.service.DummyEmailSender.yeahNah, count=1, avg=128, max=128, sum=128, dur=10, err.count=0
14:01:00, tm, org.example.myapp.service.Muse.iDoTheRealWorkAroundHere, count=1, avg=500084, max=500084, sum=500084, dur=10, err.count=0
14:01:00, tm, org.example.myapp.service.Muse.notParticularlyResistant, count=1, avg=2, max=2, sum=2, dur=10, err.count=0
14:01:00, tm, org.example.myapp.service.Muse.notThatResistant, count=1, avg=4, max=4, sum=4, dur=10, err.count=0
14:01:00, tm, org.example.myapp.service.Muse.originOfSymmetry, count=1, avg=1075, max=1075, sum=1075, dur=10, err.count=0
14:01:00, tm, org.example.myapp.service.Muse.resistance, count=1, avg=500157, max=500157, sum=500157, dur=10, err.count=0
14:01:00, tm, org.example.myapp.service.MusicLayer.playItSon, count=1, avg=611869, max=611869, sum=611869, dur=10, err.count=0
14:01:00, tm, org.example.myapp.service.RadioHead.kidA, count=1, avg=51234, max=51234, sum=51234, dur=10, err.count=0
14:01:00, tm, org.example.myapp.service.RadioHead.pabloHoney, count=1, avg=550258, max=550258, sum=550258, dur=10, err.count=0
14:01:00, tm, org.example.myapp.service.RadioHead.theBends, count=1, avg=560440, max=560440, sum=560440, dur=10, err.count=0
14:01:00, tm, org.example.myapp.web.api.CustomerResource.asBean, count=1, avg=612154, max=612154, sum=612154, dur=10, err.count=0
14:01:00, tm, org.example.myapp.web.api.CustomerResource.hello, count=1, avg=284, max=284, sum=284, dur=10, err.count=0
14:01:00, tm, org.example.myapp.web.api.MetricResource.collecting, count=1, avg=382, max=382, sum=382, dur=10, err.count=0
```

## Example Per Request output 

> Per Request timing is a little bit more expensive to collect and can produce a lot of output. As such it is expected that you only turn it on when needed. For example, for the next 5 invocations of CustomerResource.asBean() collect per request timings.

Per request timing can be set for specific timing metrics - for example, collect per request timing on the next 5 invocations of the CustomerResource.asBean() method. 

Per request timing output shows the nested calls and where the time went for that single request. The p column shows the percentage of total execution - for example 81% of execution time was taken in Muse.iDoTheRealWorkAroundHere.  Typically in looking at this output you ignore/remove/collapse anything that has percentage of 0.

The columns are: d=depth, p=percentage, ms=milliseconds, us=microseconds, m=metric name

```console
14:00:20  exe:612ms  metric:org.example.myapp.web.api.CustomerResource.asBean
   d:0    p:100  ms:612       us:612091       m:org.example.myapp.web.api.CustomerResource.asBean
   d:1    p:99   ms:611       us:611886          m:org.example.myapp.service.MusicLayer.playItSon
   d:2    p:0    ms:0         us:125                m:org.example.myapp.service.DummyEmailSender.send
   d:3    p:0    ms:0         us:106                   m:org.example.myapp.service.DummyEmailSender.yeahNah
   d:2    p:8    ms:51        us:51179              m:org.example.myapp.service.RadioHead.kidA
   d:3    p:0    ms:1         us:1072                  m:org.example.myapp.service.Muse.originOfSymmetry
   d:2    p:91   ms:560       us:560546             m:org.example.myapp.service.RadioHead.theBends
   d:3    p:89   ms:550       us:550377                m:org.example.myapp.service.RadioHead.pabloHoney
   d:4    p:81   ms:500       us:500204                   m:org.example.myapp.service.Muse.resistance
   d:5    p:0    ms:0         us:33                          m:org.example.myapp.service.Muse.notThatResistant
   d:5    p:81   ms:500       us:500108                      m:org.example.myapp.service.Muse.iDoTheRealWorkAroundHere
   d:5    p:0    ms:0         us:11                          m:org.example.myapp.service.Muse.notParticularlyResistant
```
CustomerResource.asBean took 612 milliseconds to execute. If you look at Muse.iDoTheRealWorkAroundHere it took 81% of the total execution time (500 milliseconds, 500204 microseconds). 


