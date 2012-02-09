package foodtruck.model;

import javax.annotation.Nullable;

import com.google.common.collect.Ordering;

/**
 * @author aviolette@gmail.com
 * @since 2/8/12
 */
public class TruckStatus {
  public static Ordering<TruckStatus> BY_NAME = new Ordering<TruckStatus>() {
    @Override
    public int compare(@Nullable TruckStatus left, @Nullable TruckStatus right) {
      return left.getTruck().getName().compareTo(right.getTruck().getName());
    }
  };

  private final TruckStop previousStop;
  private final TruckStop currentStop;
  private final Truck truck;
  private final boolean active;

  public TruckStatus(Truck truck, boolean isActive, @Nullable TruckStop current,
      @Nullable TruckStop previousStop) {
    this.active = isActive;
    this.truck = truck;
    this.currentStop = current;
    this.previousStop = previousStop;
  }

  public boolean isActive() {
    return active;
  }

  public TruckStop getPreviousStop() {
    return previousStop;
  }

  public TruckStop getCurrentStop() {
    return currentStop;
  }

  public Truck getTruck() {
    return truck;
  }
}
