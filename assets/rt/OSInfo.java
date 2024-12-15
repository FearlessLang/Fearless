package rt;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class OSInfo {
  private static final OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
  /**
   * Total amount of RAM on the system in MiB
   */
  public static final long TOTAL_MEMORY = os.getTotalMemorySize() >> 10 >> 10;
//  public static final long TOTAL_MEMORY = 4096;
  public static final long CPUS = Runtime.getRuntime().availableProcessors();

  /**
   * Calculate a value that scales with memory and split per logical CPU.
   * This is useful for buffer sizes, maximum thread counts, etc.
   */
  public static double memoryAndCpuScaledValue(int weight) {
    var maxRam = TOTAL_MEMORY * 0.8;
    assert maxRam >= 1 : "Total memory is too low";
    var res = (maxRam * weight) / CPUS;
    assert res >= 1;
    return res;
  }
}
