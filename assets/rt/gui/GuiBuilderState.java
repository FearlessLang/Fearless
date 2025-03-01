package rt.gui;

import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import base.Error_0;
import rt.Str;

public class GuiBuilderState {
  
  public List<String> keyPressedList = Collections.synchronizedList(new ArrayList<String>());
  public List<String> keyReleasedList = Collections.synchronizedList(new ArrayList<String>());
  public Consumer<WindowEvent> windowCloseHandler = e -> {};
  public List<AnimatedCanvas> animatedCanvases = new ArrayList<>();
  public List<Commitable> commitables = new ArrayList<>();
  public Object topPanel;
  public ScheduledExecutorService dataUpdateExecutor = Executors.newSingleThreadScheduledExecutor();
  private ScheduledExecutorService taskExecutor = Executors.newSingleThreadScheduledExecutor();
  private final CompletableFuture<Void> terminal = new CompletableFuture<Void>();
  
  public Future<Void> submitModelTask(Runnable run ) {
    return taskExecutor.submit(()->{
      try {run.run();}
      catch(Throwable e){
        taskExecutor.shutdownNow();
        terminal.completeExceptionally(e);}
      return null;
      });   
  }
  
  public void waitForCompletion() {
    try {terminal.get();}
    catch (Throwable e) { Error_0.$self.msg$imm(Str.fromJavaStr(e.getMessage()));}
  }
  
  public void complete() {
    terminal.complete(null);
  }
}
