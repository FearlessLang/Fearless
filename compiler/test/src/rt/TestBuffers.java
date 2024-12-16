package rt;

import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBuffers {
  private interface Buffer {
    Object take();
    void offer(Object x);
  }
  private static class BoundedBuffer implements Buffer {
    private static class Node {
      final Object value;
      volatile Node prev;
      volatile Node next;
      Node(Object value) {
        this.value = value;
      }
    }

    private Node head = null;
    private Node tail = null;
    private final int capacity;
    private int size = 0;

    ReentrantLock lock = new ReentrantLock();
    Condition nonEmpty = lock.newCondition();
    public BoundedBuffer(int capacity) {
      this.capacity = capacity;
      if (capacity <= 0) {
        throw new IllegalArgumentException("Capacity must be greater than 0");
      }
    }
    @Override public Object take() {
      try {
        lock.lock();
        if (head == null) {
          try {nonEmpty.await();} catch (InterruptedException e) {throw new RuntimeException(e);}
        }
        size--;
        var x = head.value;
        head = head.next;
        if (head == null) { tail = null; }
        return x;
      } finally {
        lock.unlock();
      }
    }
    @Override public void offer(Object x) {
      try {
        lock.lock();
        var node = new Node(x);
        node.prev = tail;
        node.next = null;
        if (head == null) { head = node; }
        if (tail != null) {
          tail.next = node;
        }
        tail = node;
        if (size >= capacity) {
          var newHead = head.next;
          head = newHead;
          newHead.prev = null;
          size--;
        }
        size++;
        nonEmpty.signalAll();
      } finally {
        lock.unlock();
      }
    }
  }
  private static class UnboundedBuffer implements Buffer {
    private final BlockingDeque<Object> buffer = new LinkedBlockingDeque<>();
    @Override public Object take() {
      return buffer.pollFirst();
    }
    @Override public void offer(Object x) {
      buffer.offerLast(x);
    }
  }

  @Test void unboundedBufferWorksAsQueue() {
    var buffer = new UnboundedBuffer();
    buffer.offer(1);
    buffer.offer(2);
    buffer.offer(3);
    assertEquals(1, buffer.take());
    assertEquals(2, buffer.take());
    assertEquals(3, buffer.take());
  }

  @Test void boundedBufferWorksAsQueue() {
    var buffer = new BoundedBuffer(3);
    buffer.offer(1);
    buffer.offer(2);
    buffer.offer(3);
    assertEquals(1, buffer.take());
    assertEquals(2, buffer.take());
    assertEquals(3, buffer.take());
  }
  @Test void boundedBufferOverflow() {
    var buffer = new BoundedBuffer(3);
    buffer.offer(1);
    buffer.offer(2);
    buffer.offer(3);
    buffer.offer(4);
    assertEquals(2, buffer.take());
    assertEquals(3, buffer.take());
    assertEquals(4, buffer.take());
  }
  @Test void boundedBufferOverflowMany() {
    var buffer = new BoundedBuffer(3);
    buffer.offer(1);
    buffer.offer(2);
    buffer.offer(3);
    buffer.offer(4);
    buffer.offer(5);
    buffer.offer(6);
    assertEquals(4, buffer.take());
    assertEquals(5, buffer.take());
    assertEquals(6, buffer.take());
  }
  @Test void boundedBufferOverflowManySplit() {
    var buffer = new BoundedBuffer(3);
    buffer.offer(1);
    buffer.offer(2);
    buffer.offer(3);
    buffer.offer(4);
    assertEquals(2, buffer.take());
    assertEquals(3, buffer.take());
    assertEquals(4, buffer.take());
    buffer.offer(5);
    assertEquals(5, buffer.take());
    buffer.offer(6);
    buffer.offer(7);
    buffer.offer(8);
    assertEquals(6, buffer.take());
    assertEquals(7, buffer.take());
    assertEquals(8, buffer.take());
  }
  @Test void boundedBufferOneCapacity() {
    var buffer = new BoundedBuffer(1);
    buffer.offer(1);
    buffer.offer(2);
    buffer.offer(3);
    buffer.offer(4);
    assertEquals(4, buffer.take());
    buffer.offer(5);
    assertEquals(5, buffer.take());
    buffer.offer(6);
    buffer.offer(7);
    buffer.offer(8);
    assertEquals(8, buffer.take());
  }
  @Test void boundedBufferThreaded() {
    var buffer = new BoundedBuffer(3);
    buffer.offer(1);
    buffer.offer(2);
    buffer.offer(3);
    buffer.offer(4);
    assertEquals(2, buffer.take());
    assertEquals(3, buffer.take());
    assertEquals(4, buffer.take());
    var res = new CompletableFuture<Integer>();
    Thread.ofVirtual().start(()->res.complete((int)buffer.take()));
    Thread.ofVirtual().start(()->{
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      buffer.offer(5);
    });
    assertEquals(5, res.join());
  }
}
