avaje-metric-core
=================

This is effectively a fork and refactor of https://github.com/codahale/metrics.

Why fork codahale/metrics?
--------------------------
- Architectural difference to push statistics calculations into a background thread. 
In avaje-metric-core the events are typically put into a ConcurrentLinkedQueue and a background 
thread is used to pull out the 'unprocessed' events and do the statistical 
calculations for moving averages and histogram etc. 
This is a trade off generating more GC but reducing overhead on the processing thread.

- Provide separate error and non-error metric collection for "Timed Events". This is
useful when collecting metrics where errors might have quite a different value characteristics 
(as in the case of soap operations and database operations etc). This means the 'statistics' for
error events are keep separate from the 'normal behaviour statistics'. It also means you can 
easily monitor the error rate (error count to success count) for each metric.

- Although the Moving Averages and Histogram in codahale metrics are good, once you go to 
collecting and reporting metrics every minute then in my opinion they become less valuable compared
with simple aggregate statistics (min, avg, max, count etc). The one minute Moving Average is quite 
laggy relative to the actual aggregate statistics collected and reset every minute. If you didn't
collect and report the statistics every minute (or more frequently) then the Moving Averages would 
be great but the intention for avaje-metrics is to collect and report the statistics every minute by default. 
Similarly Histograms look good but with min and max collected and reported every minute you can get a
similar value with a much simpler approach - note that for SLA's you can relatively cheaply count every 
event that exceeds a certain SLA value. In summary avaje-metric has moved away from Moving Averages and
Histogram in favour of regular collection and reporting of simple aggregate statistics.


 

License
-------

Published under Apache Software License 2.0, see LICENSE

Also refer to https://github.com/codahale/metrics.
