package io.avaje.metrics.core;


import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FileLinesTest {

  @Test
  public void exists() {

    FileLines fileLines = new FileLines("pom.xml");
    assertThat(fileLines.exists()).isTrue();
  }

  @Test
  public void exists_when_doesNotExist() {

    FileLines fileLines = new FileLines("pom.xml.doesNotExist");
    assertThat(fileLines.exists()).isFalse();
  }

  @Test
  public void lines() {

    FileLines fileLines = new FileLines("pom.xml");
    final List<String> lines = fileLines.readLines();

    assertThat(lines.size()).isGreaterThan(20);
  }


  @Test
  public void lines_whenError_expect_emptyList() {

    FileLines fileLines = new FileLines("pom.xml.doesNotExist");
    assertThat(fileLines.exists()).isFalse();

    // get lines anyway
    assertThat(fileLines.readLines()).isEmpty();
  }

  @Test
  public void linux_only_cpuUsageMicros() {

    FileLines source = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpuacct.usage");
    if (source.exists()) {
      final long micros = source.singleMicros();
      assertThat(micros).isGreaterThan(0);
    }
  }

  @Test
  public void linux_only_cpuThrottleMicros() {

    FileLines source = new FileLines("/sys/fs/cgroup/cpu,cpuacct/cpu.stat");
    if (source.exists()) {
      final List<String> lines = source.readLines();
      assertThat(lines).hasSize(3);
      assertThat(lines.toString()).contains("throttled_time");
    }
  }
}