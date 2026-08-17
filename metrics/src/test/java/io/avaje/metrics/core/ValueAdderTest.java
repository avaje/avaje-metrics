package io.avaje.metrics.core;

import io.avaje.metrics.CollectionMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueAdderTest {

  @Test
  void cumulativeAndDeltaReadsAreIndependent() {
    var adder = new ValueAdder();
    adder.add(100);
    adder.add(50);

    assertEquals(150, adder.get(CollectionMode.CUMULATIVE));
    assertEquals(150, adder.get(CollectionMode.DELTA));
    assertEquals(150, adder.get(CollectionMode.CUMULATIVE));
    assertEquals(0, adder.get(CollectionMode.DELTA));

    adder.add(25);

    assertEquals(175, adder.get(CollectionMode.CUMULATIVE));
    assertEquals(25, adder.get(CollectionMode.DELTA));
  }

  @Test
  void resetClearsCumulativeAndDeltaState() {
    var adder = new ValueAdder();
    adder.add(100);
    adder.get(CollectionMode.DELTA);

    adder.reset();

    assertEquals(0, adder.get(CollectionMode.CUMULATIVE));
    assertEquals(0, adder.get(CollectionMode.DELTA));
  }
}
