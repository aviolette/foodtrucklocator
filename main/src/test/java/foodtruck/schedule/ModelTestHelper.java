package foodtruck.schedule;

import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;

import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TwitterNotificationAccount;

/**
 * @author aviolette
 * @since 11/6/17
 */
public class ModelTestHelper {

  public static Truck truck1() {
    return Truck.builder()
        .name("Truck 1")
        .twitterHandle("truck1")
        .twitterToken("foo")
        .twitterTokenSecret("bar")
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

  public static Truck beaversDonuts() {
    return Truck.builder()
        .name("Beavers Donuts")
        .twitterHandle("beaversdonuts")
        .id("beaversdonuts")
        .build();
  }

  public static Location clarkAndMonroe() {
    return Location.builder()
        .name("Clark and Monroe, Chicago, IL")
        .valid(true)
        .lat(41.880306)
        .key("1234")
        .lng(-87.630845)
        .radius(0.07)
        .build();
  }

  public static Location wackerAndAdams() {
    return Location.builder()
        .name("Wacker and Adams, Chicago, IL")
        .valid(true)
        .radius(0.1)
        .key(456)
        .lat(41.879385)
        .lng(-87.63697499999999)
        .build();
  }

  public static Location aon() {
    return Location.builder()
        .name("AON")
        .valid(true)
        .radius(0.1)
        .lat(41.884851999999995)
        .lng(-87.620764)
        .build();
  }

  public static Location unresolvedLocation() {
    return Location.builder()
        .lat(0)
        .lng(0)
        .build();

  }

  public static TwitterNotificationAccount clarkAndMonroeTwitterAccount() {
    return TwitterNotificationAccount.builder()
        .location(ModelTestHelper.clarkAndMonroe())
        .name("Clark and Monroe")
        .oauthToken("foo")
        .oauthTokenSecret("bar")
        .twitterHandle("chiftf_125clark")
        .active(false)
        .build();
  }

  public static TwitterNotificationAccount wackerAndAdamsTwitterAccount() {
    return TwitterNotificationAccount.builder()
        .location(ModelTestHelper.wackerAndAdams())
        .name("Wacker and Adams")
        .oauthToken("foo")
        .oauthTokenSecret("bar")
        .active(true)
        .twitterHandle("chiftf_willis")
        .build();
  }

  public static TwitterNotificationAccount aonTwitterAccount() {
    return TwitterNotificationAccount.builder()
        .location(ModelTestHelper.aon())
        .name("AON")
        .oauthToken("foo")
        .oauthTokenSecret("bar")
        .active(true)
        .twitterHandle("chiftf_aon")
        .build();
  }

  public static DateTime breakfastStart() {
    return new DateTime(2016, 9, 1, 7, 0);
  }

  public static DateTime breakfastEnd() {
    return new DateTime(2016, 9, 1, 10, 0);
  }
}
