package io.avaje.metrics.core;

import java.util.ArrayList;

final class ArrayStack<E> {

  private final ArrayList<E> list;

  ArrayStack() {
    this.list = new ArrayList<>();
  }

  /**
   * Clear the stack.
   */
  void clear() {
    this.list.clear();
  }

  /**
   * Pushes an item onto the top of this stack.
   */
  void push(E item) {
    list.add(item);
  }

  /**
   * Removes the object at the top of this stack and returns that object as
   * the value of this function.
   */
  E pop() {
    return list.remove(list.size() - 1);
  }

  /**
   * Tests if this stack is empty.
   */
  boolean isEmpty() {
    return list.isEmpty();
  }

}
