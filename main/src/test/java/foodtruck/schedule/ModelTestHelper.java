package foodtruck.schedule;

import com.google.common.collect.ImmutableSet;

import foodtruck.model.Location;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 11/6/17
 */
public class ModelTestHelper {

  public static Truck truck1() {
    return Truck.builder()
        .name("Truck 1")
        .twitterHandle("truck1")
        .categories(ImmutableSet.of("Lunch", "Italian"))
        .id("truck1")
        .build();
  }

  public static Truck truck2() {
    return Truck.builder()
        .name("Truck 2")
        .twitterHandle("truck2")
        .categories(ImmutableSet.of("Donuts", "Breakfast"))
        .id("truck2")
        .build();
  }

  public static Truck truck3() {
    return Truck.builder()
        .name("Truck 3")
        .twitterHandle("truck3")
        .categories(ImmutableSet.of("Donuts", "Breakfast"))
        .id("truck3")
        .build();
  }

  public static Truck truck4() {
    return Truck.builder()
        .name("Truck 4")
        .twitterHandle("truck4")
        .categories(ImmutableSet.of("Donuts", "Breakfast"))
        .id("truck4")
        .build();
  }

  public static Location clarkAndMonroe() {
    return Location.builder()
        .name("Clark and Monroe, Chicago, IL")
        .lat(41.880306)
        .lng(-87.630845)
        .radius(0.07)
        .build();
  }

  public static Location wackerAndAdams() {
    return Location.builder()
        .name("Wacker and Adams, Chicago, IL")
        .radius(0.1)
        .lat(41.879385)
        .lng(-87.63697499999999)
        .build();
  }
}
