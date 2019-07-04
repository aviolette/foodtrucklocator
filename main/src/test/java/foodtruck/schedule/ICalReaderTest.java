package foodtruck.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.schedule.AbstractReaderTest.CHICAGO;
import static foodtruck.schedule.ModelTestHelper.wackerAndAdams;

/**
 * @author aviolette
 * @since 2019-01-16
 */
@RunWith(MockitoJUnitRunner.class)
public class ICalReaderTest extends Mockito {

  @Mock GeoLocator geoLocator;

  @Test
  public void parse() {
    ICalReader reader = new ICalReader(CHICAGO, geoLocator);
    String doc = "INFO: BEGIN:VCALENDAR\n" + "VERSION:2.0\n" + "PRODID:-//Squarespace Inc/Squarespace 6//v6//EN\n" +
        "BEGIN:VEVENT\n" + "UID:5c3e5dd8575d1f82ff8629ea@squarespace.com\n" + "DTSTAMP:20190115T222918Z\n" +
        "DTSTART:20190117T163000Z\n" + "DTEND:20190117T200000Z\n" + "SUMMARY:Chicago Lunch\n" +
        "GEO:40.720756;-74.000761\n" + "LOCATION:100 South Wacker Drive\\, Chicago\\, IL\\, 60606\\, United States\n" +
        "END:VEVENT\n" + "END:VCALENDAR";
    when(geoLocator.locateOpt("Chicago Lunch")).thenReturn(Optional.empty());
    Location loc = wackerAndAdams();
    when(geoLocator.locateOpt("100 South Wacker Drive, Chicago, IL, 60606, United States")).thenReturn(
        Optional.of(loc));
    List<ICalReader.ICalEvent> events = reader.parse(doc, true);
    assertThat(events).containsExactly(
        new ICalReader.ICalEvent.Builder().start(ZonedDateTime.of(2019, 1, 17, 16, 30, 0, 0, ZoneOffset.UTC))
            .end(ZonedDateTime.of(2019, 1, 17, 20, 0, 0, 0, ZoneOffset.UTC))
            .summary("Chicago Lunch")
            .location(loc)
            .build());
  }

  @Test
  public void parse2() throws IOException {
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("church.ics");
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);
    when(geoLocator.locateOpt(any())).thenReturn(Optional.empty());
    ICalReader reader = new ICalReader(CHICAGO, geoLocator);
    List<ICalReader.ICalEvent> events = reader.parse(doc, true);
    assertThat(events).hasSize(5);
    assertThat(events).contains(new ICalReader.ICalEvent.Builder()
        .start(ZonedDateTime.of(2019, 1, 30, 0, 0, 0, 0, CHICAGO))
        .end(ZonedDateTime.of(2019, 1, 31, 0, 0, 0, 0, CHICAGO))
        .summary("Brrrr! Too Cold.  Tap Room closed today.")
        .build());
  }

  @Test
  public void toasty() throws IOException {
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("toasty.ics");
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);
    when(geoLocator.locateOpt(any())).thenReturn(Optional.empty());
    when(geoLocator.locateOpt("Brickyards Park, 375 Elm St., Deerfield, IL, 60015, United States")).thenReturn(Optional.of(wackerAndAdams()));
    ICalReader reader = new ICalReader(CHICAGO, geoLocator);
    List<ICalReader.ICalEvent> events = reader.parse(doc, true);
    assertThat(events).hasSize(21);
    assertThat(events).contains(ICalReader.ICalEvent
        .builder()
        .start(ZonedDateTime.of(2019, 7, 4, 8, 0, 0, 0, ZoneOffset.ofHours(-5)))
        .end(ZonedDateTime.of(2019, 7, 4, 17, 0, 0, 0, ZoneOffset.ofHours(-5)))
        .summary("Deerfield Family Days")
        .location(wackerAndAdams())
        .categories(ImmutableList.of("The Crave Bar","Toasty Cheese"))
        .build());
  }
  @Test
  public void toasty2() throws IOException {
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("toasty2.ics");
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);
    when(geoLocator.locateOpt(any())).thenReturn(Optional.empty());
    when(geoLocator.locateOpt("Brickyards Park, 375 Elm St., Deerfield, IL, 60015, United States")).thenReturn(Optional.of(wackerAndAdams()));
    ICalReader reader = new ICalReader(CHICAGO, geoLocator);
    List<ICalReader.ICalEvent> events = reader.parse(doc, true);
    assertThat(events).hasSize(13);
    assertThat(events).contains(ICalReader.ICalEvent
        .builder()
        .start(ZonedDateTime.of(2019, 7, 4, 8, 0, 0, 0, ZoneOffset.ofHours(-5)))
        .end(ZonedDateTime.of(2019, 7, 4, 17, 0, 0, 0, ZoneOffset.ofHours(-5)))
        .summary("Deerfield Family Days")
        .location(wackerAndAdams())
        .categories(ImmutableList.of("The Crave Bar","Toasty Cheese"))
        .build());
  }
}