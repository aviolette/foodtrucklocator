package foodtruck.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.dao.TruckDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;
import foodtruck.util.FakeClock;

import static com.google.common.truth.Truth.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class YourSistersReaderTest extends Mockito {

  private static final Truck TRUCK = Truck.builder()
      .id("yoursisterstomato")
      .name("Your Sisters Tomato")
      .build();
  public static final ZoneId ZONE = ZoneId.of("America/Chicago");
  private YourSistersReader reader;

  @Mock private TruckDAO truckDAO;
  @Mock private AddressExtractor extractor;
  @Mock private GeoLocator geoLocator;

  @Before
  public void before() {
    this.reader = new YourSistersReader(extractor, truckDAO, ZONE, FakeClock.fixed(1560515802000L), geoLocator);
  }

  @Test
  public void findStops() throws IOException {
    when(truckDAO.findByIdOpt("yoursisterstomato")).thenReturn(Optional.of(TRUCK));
    when(geoLocator.locateOpt(any())).thenReturn(Optional.empty());
    when(geoLocator.locateOpt("2500 N. Woodland Park, Buffalo Grove, IL")).thenReturn(Optional.of(Location.builder()
        .name("2500 N. Woodland Park, Buffalo Grove, IL")
        .valid(true)
        .lat(40)
        .lng(-80)
        .wasJustResolved(true)
        .build()));
    when(extractor.parse(" Woodland Park 2500 N. Buffalo Grove Rd, Buffalo Grove", TRUCK)).thenReturn(
        ImmutableList.of("2500 N. Woodland Park, Buffalo Grove, IL"));
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("yoursisters.html");
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);
    List<TempTruckStop> stops = reader.findStops(doc);
    assertThat(stops).hasSize(1);
    assertThat(stops).contains(TempTruckStop.builder()
        .locationName("2500 N. Woodland Park, Buffalo Grove, IL")
        .calendarName("yoursisterstomato")
        .truckId("yoursisterstomato")
        .startTime(ZonedDateTime.of(2019, 6, 1, 12, 0, 0, 0, ZONE))
        .endTime(ZonedDateTime.of(2019, 6, 1, 16, 0, 0, 0, ZONE))
        .build());
  }
}