package org.avaje.metric.core;

/**
 * Memory warning event.
 */
public class MemoryWarnEvent {

	private final long memoryInMB;
	private final long warnLevel;
	private final long lastAlertLevel;
	private final String pid;

	/**
	 * Create a memory warning event.
	 */
	public MemoryWarnEvent(long memoryInMB, long warnLevel, long lastAlertLevel, String pid) {
		this.memoryInMB = memoryInMB;
		this.warnLevel = warnLevel;
		this.lastAlertLevel = lastAlertLevel;
		this.pid = pid;
	}

	/**
	 * Return the current memory used in MB.
	 */
	public long getMemoryInMB() {
		return memoryInMB;
	}

	/**
	 * Return the memory warn level in MB.
	 */
	public long getWarnLevel() {
		return warnLevel;
	}

	/**
	 * Return the last memory level that was alerted.
	 */
	public long getLastAlertLevel() {
		return lastAlertLevel;
	}

	/**
	 * Return the process id.
	 */
	public String getPid() {
		return pid;
	}
}
