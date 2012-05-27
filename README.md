avaje-metric-core
=================

This is effectively a fork and refactor of https://github.com/codahale/metrics.

Why fork codahale/metrics?
--------------------------
- Provide separate error and non-error metric collection for "Timed Events". This is
useful when collecting metrics of soap operations.

- Push Histogram and rate statistics calculations into a background thread. 
In avaje-metric-corethe TimedMetric is put into a ConcurrentLinkedQueue and a background 
thread is used to pull out the 'unprocessed' TimedMetric events and do the statistical 
calculations using Histogram etc. So the trade off is more GC but less overhead on the 
processing thread.

 

License
-------

Published under Apache Software License 2.0, see LICENSE

Also refer to https://github.com/codahale/metrics.
