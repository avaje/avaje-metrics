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
error events are keep separate from the 'normal behaviour statistics'.

- Orientated towards using 'Moving Averages' and 'Moving Summary Statistics' (providing the min max
range of values over the last 5 minutes) rather than Histogram. The goal is to be able to provide
meaningful statistics for the 'very current activity' (last 10 seconds, what is happening now) as well
as 'last 5 minutes of activity' and thus be able to report/log less frequently - report/log activity 
every 1 minute or report/log activity every 5 minutes.


 

License
-------

Published under Apache Software License 2.0, see LICENSE

Also refer to https://github.com/codahale/metrics.
