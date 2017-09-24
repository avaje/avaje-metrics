package org.avaje.metric.jvm;

/**
 * Handle alerting when memory warning level hit.
 */
public interface MemoryWarnHandler {

	/**
	 * Hit memory warning limit.
	 */
	void warning(MemoryWarnEvent event);

	/**
	 * Still at memory warning limit.
	 */
	void stillWarning(MemoryWarnEvent event);
}
