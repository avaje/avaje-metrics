/**
 * The main public API for Avaje Metrics.
 * <p>
 * Avaje metrics provides various metrics for.
 * <ul>
 * <li> Timers - used to collect timing statistics on methods or sections of code.</li>
 * <li> Gauges - used to monitor resource values like memory (used, max etc) and threads (active, max etc).</li>
 * <li> Counters - count events like number of login events, number of errors logged.</li>
 * <li> Value - used to collect statistics on values that aggregate like total bytes sent etc.
 * </ul>
 * <p>
 * Each of the metrics collect statistics. Generally a MetricReporter is used to periodically collect and report
 * the statistics collected.
 */
package io.avaje.metrics;