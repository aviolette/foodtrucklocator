package foodtruck.schedule;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.schedule.AbstractReaderTest.CHICAGO;

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
    Location loc = Location.builder().name("100 South Wacker Drive, Chicago, IL, 60606, United States").build();
    when(geoLocator.locateOpt("100 South Wacker Drive, Chicago, IL, 60606, United States")).thenReturn(Optional.of(loc));
    List<ICalReader.ICalEvent> events = reader.parse(doc, true);
    assertThat(events).containsExactly(new ICalReader.ICalEvent(ZonedDateTime.of(2019, 1, 17, 16, 30, 0, 0, ZoneOffset.UTC), ZonedDateTime.of(2019, 1, 17, 20, 0, 0, 0,
        ZoneOffset.UTC), "Chicago Lunch", null, loc));
  }
}