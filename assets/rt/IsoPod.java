package rt;

import base.*;
import base.caps.HistorySizeMatch_1;
import base.caps.IsoPodFlowOpts_0;
import base.caps.IsoViewer_2;
import base.flows.*;
import rt.flows.FlowCreator;

import java.util.IdentityHashMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class IsoPod implements base.caps._MagicIsoPodImpl_1 {
  private static final Info_0 consumeErrorInfo = Infos_0.$self.msg$imm(Str.fromJavaStr("Cannot consume an empty IsoPod."));

  private Object x;
  private final IdentityHashMap<FlowOp, FlowOp> flows = new IdentityHashMap<>();
  public IsoPod(Object x) {
    assert x != null;
    this.x = x;
  }

  @Override public Object $exclamation$mut() {
    if (x == null) {
      throw new FearlessError(consumeErrorInfo);
    }
    var res = x;
    x = null;
    flows.values().forEach(FlowOp::stop$mut);
    flows.clear();
    return res;
  }

  @Override public Object peek$read(IsoViewer_2 f_m$) {
    return x == null ? f_m$.empty$mut() : f_m$.some$mut(x);
  }

  @Override public Void_0 next$mut(Object val_m$) {
    this.x = val_m$;
    flows.values().forEach(f->f.next(val_m$));
    return base.Void_0.$self;
  }

  @Override public Bool_0 isAlive$read() {
    return x != null ? True_0.$self : False_0.$self;
  }

  @Override public Void_0 flow$mut(F_2 f_m$) {
    var op = new FlowOp(new UnboundedBuffer());
    flows.put(op, op);
    var flow = (Flow_1)f_m$.$hash$read(FlowCreator.fromFlowOp(_SeqFlow_0.$self, op, -1));
    var flowOp = flow.unwrapOp$mut(_UnwrapFlowToken_0.$self);
    Thread.ofVirtual().start(()->flowOp.forRemaining$mut(new _Sink_1() {
      @Override public Void_0 stop$mut() {
        return Void_0.$self;
      }
      @Override public Void_0 pushError$mut(Info_0 info_m$) {
        throw new RuntimeException(new String(info_m$.str$imm().utf8().array()));
      }
      @Override public Void_0 $hash$mut(Object x_m$) {
        System.out.println("flow$mut: "+x_m$);
        return Void_0.$self;
      }
    }));
    op.next(x);
    return Void_0.$self;
  }

  @Override public Flow_1 flow$mut(F_2 f_m$, IsoPodFlowOpts_0 opts_m$) {
    var buffer = (Buffer) opts_m$.historySize$imm().match$imm(new HistorySizeMatch_1() {
      @Override public Object unbounded$mut() {
        return new UnboundedBuffer();
      }
      @Override public Object bounded$mut(long n_m$) {
        return new BoundedBuffer(n_m$);
      }
    });
    var op = new FlowOp(buffer);
    flows.put(op, op);
    var flow = (Flow_1)f_m$.$hash$read(FlowCreator.fromFlowOp(_SeqFlow_0.$self, op, -1));
    Thread.ofVirtual().start(()->flow.unwrapOp$mut(_UnwrapFlowToken_0.$self).forRemaining$mut(new _Sink_1() {
      @Override public Void_0 stop$mut() {
        return Void_0.$self;
      }
      @Override public Void_0 pushError$mut(Info_0 info_m$) {
        throw new RuntimeException(new String(info_m$.str$imm().utf8().array()));
      }
      @Override public Void_0 $hash$mut(Object x_m$) {
        System.out.println("flow$mut: "+x_m$);
        return Void_0.$self;
      }
    }));
    op.next(x);
    return flow;
  }

  private static class FlowOp implements FlowOp_1 {
    private final Buffer buffer;
    private boolean isRunning = true;
    FlowOp(Buffer buffer) {
      this.buffer = buffer;
    }

    public void next(Object e) {
      buffer.offer(e);
    }
    @Override public Bool_0 isFinite$mut() {
      return False_0.$self;
    }
    @Override public Void_0 step$mut(_Sink_1 sink_m$) {
      sink_m$.$hash$mut(buffer.take());
      return Void_0.$self;
    }
    @Override public Void_0 stop$mut() {
      isRunning = false;
      return Void_0.$self;
    }
    @Override public Bool_0 isRunning$mut() {
      return isRunning ? True_0.$self : False_0.$self;
    }
    @Override public Void_0 forRemaining$mut(_Sink_1 downstream_m$) {
      while (isRunning) {
        downstream_m$.$hash$mut(buffer.take());
      }
      return Void_0.$self;
    }
    @Override public Opt_1 split$mut() {
      return Opt_1.$self;
    }
    @Override public Bool_0 canSplit$read() {
      return False_0.$self;
    }
  }
  private interface Buffer {
    Object take();
    void offer(Object x);
  }
  private static class BoundedBuffer implements Buffer {
    private static class Node {
      final Object value;
      Node prev;
      Node next;
      Node(Object value) {
        this.value = value;
      }
    }

    private Node head = null;
    private Node tail = null;
    private final long capacity;
    private int size = 0;

    ReentrantLock lock = new ReentrantLock();
    Condition nonEmpty = lock.newCondition();
    public BoundedBuffer(long capacity) {
      if (capacity <= 0) {
        throw new FearlessError(Infos_0.$self.msg$imm(Str.fromJavaStr("Capacity must be greater than 0")));
      }
      this.capacity = capacity;
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
      try {
        return buffer.takeFirst();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    @Override public void offer(Object x) {
      buffer.offerLast(x);
    }
  }
}
