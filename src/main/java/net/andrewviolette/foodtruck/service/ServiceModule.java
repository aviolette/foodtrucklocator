package net.andrewviolette.foodtruck.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import org.joda.time.LocalTime;

import net.andrewviolette.foodtruck.model.DayOfWeek;
import net.andrewviolette.foodtruck.model.Location;
import net.andrewviolette.foodtruck.model.ReoccurringTruckStop;
import net.andrewviolette.foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class ServiceModule extends AbstractModule {
  private static final String TAMALE_SPACESHIP_TRUCK = "tamaleSpaceshipTruck";
  private static final String TAMALESPACESHIP = "tamalespaceship";

  @Override
  protected void configure() {
    bind(FoodTruckStopService.class).to(FoodTruckStopServiceImpl.class);

  }

  @Provides
  @Named(TAMALE_SPACESHIP_TRUCK)
  public Truck provideTamaleSpaceShipTruck() {
    return new Truck(TAMALESPACESHIP, "Tamale Spaceship", null, null,
        "http://a3.twimg.com/profile_images/1402134609/be86_1_1__normal.jpg");
  }

  @Provides @DefaultStrategy
  public TwitterFeedScheduleStrategy provideTwitterStrategy() {
    return new TwitterFeedScheduleStrategy();
  }
  
  @Provides
  @Named(TAMALESPACESHIP) 
  public DeterministicScheduleStrategy provideTamaleSpaceshipSchedule(
      @Named(TAMALE_SPACESHIP_TRUCK) Truck truck) {
    List<ReoccurringTruckStop> stops = ImmutableList.of(
        new ReoccurringTruckStop(truck, DayOfWeek.monday, new LocalTime(11, 0), new LocalTime(23, 0),
            new Location(41.8806907, -87.6338027, "Wells and Monroe")),
        new ReoccurringTruckStop(truck, DayOfWeek.tuesday, new LocalTime(11, 0),
            new LocalTime(23, 0),
            new Location(41.8819485, -87.6367349, "Madison and Wacker")),
        new ReoccurringTruckStop(truck, DayOfWeek.wednesday, new LocalTime(11, 0),
            new LocalTime(23, 0),
            new Location(41.8843737, -87.6207861, "Aon Center")),
        new ReoccurringTruckStop(truck, DayOfWeek.thursday, new LocalTime(11, 0),
            new LocalTime(23, 0),
            new Location(41.8857044, -87.6413047, "Clinton and Lake")),
        new ReoccurringTruckStop(truck, DayOfWeek.friday, new LocalTime(11, 0), new LocalTime(23, 0),
            new Location(41.8807438, -87.6293867, "Dearborn and Monroe"))
    );
    return new DeterministicScheduleStrategy(stops);
  }

  @Provides
  public ConcurrentMap<String, ScheduleStrategy> provideStrategies(
      @Named(TAMALESPACESHIP) final DeterministicScheduleStrategy strategy) {
    return new ConcurrentHashMap<String, ScheduleStrategy>() {{
      put(TAMALESPACESHIP, strategy);
    }};
  }

  @Provides
  public Map<String, Truck> providesTrucks(@Named(TAMALE_SPACESHIP_TRUCK) Truck tamaleSpaceship) {
    return ImmutableMap.of(tamaleSpaceship.getId(), tamaleSpaceship);
  }
}
