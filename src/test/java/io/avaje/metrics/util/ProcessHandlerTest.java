package io.avaje.metrics.util;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ProcessHandlerTest {

  @Test
  public void testCommand() {

    boolean linux = System.getProperty("os.name").toLowerCase().contains("linux");
    if (linux) {

      String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

      ProcessResult result = ProcessHandler.command("grep", "Vm", "/proc/" + pid + "/status");

      List<String> lines = result.getStdOutLines();
      System.out.println(lines);
      assertFalse(lines.isEmpty());
    }
  }

}