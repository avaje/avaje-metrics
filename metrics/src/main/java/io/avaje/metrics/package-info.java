/**
 * The main public API for Avaje Metrics.
 * <p>
 * Avaje metrics provides various metrics for.
 * <ul>
 * <li> Timers - used to collect timing statistics on methods or sections of code.</li>
 * <li> Gauges - used to monitor resource values like memory (used, max etc) and threads (active, max etc).</li>
 * <li> Counters - count events like number of login events, number of errors logged.</li>
 * <li> Meter - used to collect statistics on events with values that aggregate like bytes sent, lines read etc.
 * </ul>
 * <p>
 * Each of the metrics collect statistics. Periodically collect and report the statistics collected.
 */
package io.avaje.metrics;
