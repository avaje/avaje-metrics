package io.avaje.metrics.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JvmProcessMemoryTest {

  @Test
  void testGetMetrics() {
    FileLines statusFile = new FileLines("src/test/resources/process/process_status");
    assertTrue(statusFile.exists());

    JvmProcessMemory.Source source = new JvmProcessMemory.Source(statusFile);
    assertThat(source.getRss()).isEqualTo(36);
    assertThat(source.getHwm()).isEqualTo(66);
  }

}
