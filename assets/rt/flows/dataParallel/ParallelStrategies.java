package rt.flows.dataParallel;

public interface ParallelStrategies {
  void seqOnly();
  void oneParOneSeq();
  void manyPar();

  default void runFlow(int nTasks) {
    if (nTasks == 2) {
//      oneParOneSeq();
      manyPar();
      return;
    }
    if (nTasks <= 3) {
      seqOnly();
      return;
    }
    manyPar();
  }
}
