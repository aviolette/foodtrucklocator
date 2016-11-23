package foodtruck.model;

import com.google.common.testing.EqualsTester;

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * @author aviolette
 * @since 11/23/16
 */
public class TrackingDeviceTest {

  @Test
  public void equals() {
    TrackingDevice.Builder builder = TrackingDevice.builder()
        .label("label")
        .deviceNumber("device number");
    DateTime dt = new DateTime(2016, 11, 22, 0, 1);
    Location location = Location.builder()
        .name("FOO")
        .lat(12)
        .lng(-13)
        .build();
    new EqualsTester().addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.degreesFromNorth(125)
            .build(), builder.degreesFromNorth(125)
            .build())
        .addEqualityGroup(builder.atBlacklistedLocation(true)
            .build(), builder.atBlacklistedLocation(true)
            .build())
        .addEqualityGroup(builder.fuelLevel("123")
            .build(), builder.fuelLevel("123")
            .build())
        .addEqualityGroup(builder.batteryCharge("12.3v")
            .build(), builder.batteryCharge("12.3v")
            .build())
        .addEqualityGroup(builder.key(123)
            .build(), builder.key(123)
            .build())
        .addEqualityGroup(builder.lastBroadcast(dt)
            .build(), builder.lastBroadcast(dt)
            .build())
        .addEqualityGroup(builder.lastLocation(location)
            .build(), builder.lastLocation(location)
            .build())
        .addEqualityGroup(builder.lastModified(dt)
            .build(), builder.lastModified(dt)
            .build())
        .addEqualityGroup(builder.truckOwnerId("foo")
            .build(), builder.truckOwnerId("foo")
            .build())
        .addEqualityGroup(builder.lastActualLocation(location).build(),
            builder.lastActualLocation(location).build())
        .testEquals();
  }
}