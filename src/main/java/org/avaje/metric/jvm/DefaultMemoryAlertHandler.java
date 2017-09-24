package org.avaje.metric.jvm;

import org.avaje.metric.util.ProcessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default memory alerting that invokes kill -3 to dump the thread stacks and memory use.
 */
class DefaultMemoryAlertHandler implements MemoryWarnHandler {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMemoryAlertHandler.class);

	private final long recent;

	private long lastAlert;

	DefaultMemoryAlertHandler(long freqInSeconds) {
		this.recent = freqInSeconds * 1000;
	}

	@Override
	public void stillWarning(MemoryWarnEvent event) {
		logger.info("still exceeding memory warning limit - memory at {} exceeds warn level {}", event.getMemoryInMB(), event.getWarnLevel());
	}

	@Override
	public void warning(MemoryWarnEvent event) {

		try {
			if (recentAlert()) {
				logger.warn("Hit memory warning limit. {} exceeds warn level {} - recently alerted", event.getMemoryInMB(), event.getWarnLevel());

			} else {
				logger.warn("Hit memory warning limit. {} exceeds warn level {} - dumping memory alert", event.getMemoryInMB(), event.getWarnLevel());
				lastAlert = System.currentTimeMillis();
				ProcessHandler.command("kill", "-3", event.getPid());
			}

		} catch (Exception e) {
			logger.warn("failed to dump memory warning", e);
		}
	}

	/**
	 * Return true if we have recently alerted such that we are not too loud.
	 */
	private boolean recentAlert() {
		return lastAlert > System.currentTimeMillis() - recent;
	}

}
