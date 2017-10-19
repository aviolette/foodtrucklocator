package foodtruck.model;

import java.util.Set;

/**
 * A truck stop decorated with stats
 * @author aviolette
 * @since 4/8/15
 */
public class TruckStopWithCounts {
  private final TruckStop stop;
  private final Set<String> truckNames;

  public TruckStopWithCounts(TruckStop stop, Set<String> truckNames) {
    this.stop = stop;
    this.truckNames = truckNames;
  }

  public TruckStop getStop() {
    return stop;
  }

  public Set<String> getTruckNames() {
    return truckNames;
  }
}
