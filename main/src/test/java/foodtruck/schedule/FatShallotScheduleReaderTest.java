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
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.time.Clock;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.schedule.AbstractReaderTest.CHICAGO;
import static foodtruck.schedule.ModelTestHelper.wackerAndAdams;
import static org.mockito.Mockito.when;

/**
 * @author aviolette
 * @since 11/10/18
 */
@RunWith(MockitoJUnitRunner.class)
public class FatShallotScheduleReaderTest {

  @Mock private AddressExtractor extractor;
  @Mock private Clock clock;
  @Mock private GeoLocator geolocator;
  private FatShallotScheduleReader parser;
  private static final Truck thefatshallot = Truck.builder()
      .id("thefatshallot")
      .name("The Fat Shallot")
      .build();

  @Before
  public void setup() {
    parser = new FatShallotScheduleReader(clock, extractor, geolocator, CHICAGO);

  }

  @Test
  public void findStops() throws IOException {
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("fatshallot.html");
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);
    when(clock.now8()).thenReturn(ZonedDateTime.of(2018, 11, 6, 11, 1,2, 3, CHICAGO));
    when(extractor.parse("Wacker & Adams 11-2pm", thefatshallot)).thenReturn(ImmutableList.of("Wacker and Adams, Chicago, IL"));
    when(extractor.parse("Wacker & Adams 11-3pm", thefatshallot)).thenReturn(ImmutableList.of("Wacker and Adams, Chicago, IL"));
    when(geolocator.locateOpt("Wacker and Adams, Chicago, IL")).thenReturn(Optional.of(wackerAndAdams()));
    List<TempTruckStop> items = parser.findStops(doc);
    assertThat(items).contains(TempTruckStop.builder()
        .startTime(ZonedDateTime.of(2018, 11, 6, 11, 0, 0, 0, CHICAGO))
        .endTime(ZonedDateTime.of(2018, 11, 6, 15, 0, 0, 0, CHICAGO))
        .locationName(wackerAndAdams().getName())
        .truckId("thefatshallot")
        .calendarName(parser.getCalendar())
        .build());
  }

  @Test
  public void findStops2() throws IOException {
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("fatshallot2.html");
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);
    when(clock.now8()).thenReturn(ZonedDateTime.of(2019, 1, 3, 11, 1,2, 3, CHICAGO));
    when(extractor.parse("Wacker & Adams 11-2pm", thefatshallot)).thenReturn(ImmutableList.of("Wacker and Adams, Chicago, IL"));
    when(extractor.parse("Wacker & Adams 11-3pm", thefatshallot)).thenReturn(ImmutableList.of("Wacker and Adams, Chicago, IL"));
    when(geolocator.locateOpt("Wacker and Adams, Chicago, IL")).thenReturn(Optional.of(wackerAndAdams()));

    List<TempTruckStop> stops = parser.findStops(doc);
    assertThat(stops).hasSize(2);

  }
}