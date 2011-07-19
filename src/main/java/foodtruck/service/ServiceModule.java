package foodtruck.service;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import foodtruck.model.Truck;
import foodtruck.schedule.ScheduleStrategy;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class ServiceModule extends AbstractModule {
  @Override
  protected void configure() {
  }

  @Provides
  public FoodTruckStopService provideFoodTruckService(FoodTruckStopServiceImpl impl) {
    return impl;
  }

  @Provides @Singleton
  public Map<String, Truck> provideTrucks(Map<Truck, ScheduleStrategy> truckStrategies) {
    ImmutableMap.Builder<String, Truck> trucks = ImmutableMap.builder();
    for (Truck truck : truckStrategies.keySet())  {
      trucks.put(truck.getId(), truck);
    }
    return trucks.build();
  }
}
