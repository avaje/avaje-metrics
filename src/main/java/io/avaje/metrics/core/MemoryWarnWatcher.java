package io.avaje.metrics.core;

/**
 * Watch and warn on process memory.
 */
public interface MemoryWarnWatcher {

	/**
	 * Invoke warnings etc on the level of process memory.
	 */
	long process(long memoryInMB);
}
