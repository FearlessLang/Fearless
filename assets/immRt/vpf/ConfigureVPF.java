package rt.vpf;

public final class ConfigureVPF {
  public static long getTasksPerCPU(int cpus) {
    var raw = System.getenv("FEARLESS_VPF_TASKS_PER_CPU");
//    if (raw == null) { return -1; }
    if (raw == null) {
//      return -1;
      return cpus * 5L;
    }
    return cpus * Long.parseLong(raw);
  }

  public static long getHeartbeatInterval() {
    if (System.getenv("FEARLESS_NO_VPF") != null) {
      return -1;
    }
    if (System.getenv("FEARLESS_HEARTBEAT_INTERVAL") != null) {
      return Long.parseLong(System.getenv("FEARLESS_HEARTBEAT_INTERVAL"));
    }

    // Automatic heartbeat configuration out of scope for this project, so we'll just return a default value
    return 1500L;
//    return -1L;
  }
  public static void main(String[] ignored) {
    System.err.println("Heartbeat interval: "+getHeartbeatInterval());
  }
}
