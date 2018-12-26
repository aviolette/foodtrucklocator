package foodtruck.schedule;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author aviolette
 * @since 10/14/18
 */
@RunWith(MockitoJUnitRunner.class)
public class ICalStopReaderTest {

  private @Mock AddressExtractor extractor;
  private @Mock GeoLocator locator;
  private ICalStopReader reader;

  @Before
  public void setup() {
    this.reader = new ICalStopReader(locator, extractor);
  }

  @Test
  public void read() {

    String input = "BEGIN:VCALENDAR\n" + "VERSION:2.0\n" +
        "PRODID:-//Smokin&#039; BBQ Kitchen - ECPv4.6.23//NONSGML v1.0//EN\n" + "CALSCALE:GREGORIAN\n" +
        "METHOD:PUBLISH\n" + "X-WR-CALNAME:Smokin&#039; BBQ Kitchen\n" +
        "X-ORIGINAL-URL:https://smokinbbqkitchen.com\n" + "X-WR-CALDESC:Events for Smokin&#039; BBQ Kitchen\n" +
        "BEGIN:VEVENT\n" + "DTSTART;TZID=America/Chicago:20181005T170000\n" +
        "DTEND;TZID=America/Chicago:20181005T210000\n" + "DTSTAMP:20181014T193152\n" + "CREATED:20181001T190226Z\n" +
        "LAST-MODIFIED:20181001T225005Z\n" + "UID:50-1538758800-1538773200@smokinbbqkitchen.com\n" +
        "SUMMARY:Pollyanna Brewing Company - Roselle\n" + "DESCRIPTION:\n" +
        "URL:https://smokinbbqkitchen.com/event/pollyanna-brewing-company/\n" +
        "LOCATION:Pollyanna Brewing Company – Roselle\\, 245 E Main St\\, Roselle\\, IL\\, 60172\\, United States\n" +
        "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTART;TZID=America/Chicago:20181006T170000\n" +
        "DTEND;TZID=America/Chicago:20181006T210000\n" + "DTSTAMP:20181014T193152\n" + "CREATED:20181001T224932Z\n" +
        "LAST-MODIFIED:20181001T225027Z\n" + "UID:58-1538845200-1538859600@smokinbbqkitchen.com\n" +
        "SUMMARY:Pollyanna Brewing Company - Lemont\n" + "DESCRIPTION:\n" +
        "URL:https://smokinbbqkitchen.com/event/pollyanna-brewing-company-2/\n" +
        "LOCATION:Pollyanna Brewing Company – Lemont\\, 431 Talcott Ave\\, Lemont\\, 60439\\, United States\n" +
        "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTART;TZID=America/Chicago:20181010T170000\n" +
        "DTEND;TZID=America/Chicago:20181010T210000\n" + "DTSTAMP:20181014T193152\n" + "CREATED:20181001T225119Z\n" +
        "LAST-MODIFIED:20181001T225119Z\n" + "UID:63-1539190800-1539205200@smokinbbqkitchen.com\n" +
        "SUMMARY:Pollyanna Brewing Company - Lemont\n" + "DESCRIPTION:Trivia Night \\n\n" +
        "URL:https://smokinbbqkitchen.com/event/pollyanna-brewing-company-lemont/\n" +
        "LOCATION:Pollyanna Brewing Company – Lemont\\, 431 Talcott Ave\\, Lemont\\, 60439\\, United States\n" +
        "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTART;TZID=America/Chicago:20181011T170000\n" +
        "DTEND;TZID=America/Chicago:20181011T210000\n" + "DTSTAMP:20181014T193152\n" + "CREATED:20181001T225327Z\n" +
        "LAST-MODIFIED:20181001T225354Z\n" + "UID:65-1539277200-1539291600@smokinbbqkitchen.com\n" +
        "SUMMARY:Imperial Oak Brewing Company\n" + "DESCRIPTION:\n" +
        "URL:https://smokinbbqkitchen.com/event/imperial-oak-brewing-company/\n" +
        "LOCATION:Imperial Oak Brewing Company\\, 501 Willow Blvd\\, Willow Springs\\, IL\\, 60172\\, United States\n" +
        "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTART;TZID=America/Chicago:20181014T130000\n" +
        "DTEND;TZID=America/Chicago:20181014T180000\n" + "DTSTAMP:20181014T193152\n" + "CREATED:20181001T225433Z\n" +
        "LAST-MODIFIED:20181001T225433Z\n" + "UID:68-1539522000-1539540000@smokinbbqkitchen.com\n" +
        "SUMMARY:Imperial Oak Brewing Company\n" + "DESCRIPTION:\n" +
        "URL:https://smokinbbqkitchen.com/event/imperial-oak-brewing-company-2/\n" +
        "LOCATION:Imperial Oak Brewing Company\\, 501 Willow Blvd\\, Willow Springs\\, IL\\, 60172\\, United States\n" +
        "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTART;TZID=America/Chicago:20181019T170000\n" +
        "DTEND;TZID=America/Chicago:20181019T210000\n" + "DTSTAMP:20181014T193152\n" + "CREATED:20181001T225926Z\n" +
        "LAST-MODIFIED:20181001T225926Z\n" + "UID:70-1539968400-1539982800@smokinbbqkitchen.com\n" +
        "SUMMARY:Miskatonic Brewing Company\n" + "DESCRIPTION:\n" +
        "URL:https://smokinbbqkitchen.com/event/miskatonic-brewing-company/\n" +
        "LOCATION:Miskatonic Brewing Company\\, 1000 N Frontage Rd. Unit C\\, Darien\\, IL\\, 60561\\, United States\n" +
        "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTART;TZID=America/Chicago:20181020T110000\n" +
        "DTEND;TZID=America/Chicago:20181020T160000\n" + "DTSTAMP:20181014T193152\n" + "CREATED:20181001T230018Z\n" +
        "LAST-MODIFIED:20181001T230018Z\n" + "UID:73-1540033200-1540051200@smokinbbqkitchen.com\n" +
        "SUMMARY:Imperial Oak Brewing Company\n" + "DESCRIPTION:\n" +
        "URL:https://smokinbbqkitchen.com/event/imperial-oak-brewing-company-3/\n" +
        "LOCATION:Imperial Oak Brewing Company\\, 501 Willow Blvd\\, Willow Springs\\, IL\\, 60172\\, United States\n" +
        "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTART;TZID=America/Chicago:20181024T170000\n" +
        "DTEND;TZID=America/Chicago:20181024T210000\n" + "DTSTAMP:20181014T193152\n" + "CREATED:20181001T230119Z\n" +
        "LAST-MODIFIED:20181001T230119Z\n" + "UID:75-1540400400-1540414800@smokinbbqkitchen.com\n" +
        "SUMMARY:Pollyanna Brewing Company - Lemont\n" + "DESCRIPTION:Triva Night \\n\n" +
        "URL:https://smokinbbqkitchen.com/event/pollyanna-brewing-company-lemont-2/\n" +
        "LOCATION:Pollyanna Brewing Company – Lemont\\, 431 Talcott Ave\\, Lemont\\, 60439\\, United States\n" +
        "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTART;TZID=America/Chicago:20181026T170000\n" +
        "DTEND;TZID=America/Chicago:20181026T210000\n" + "DTSTAMP:20181014T193152\n" + "CREATED:20181001T230253Z\n" +
        "LAST-MODIFIED:20181001T230253Z\n" + "UID:77-1540573200-1540587600@smokinbbqkitchen.com\n" +
        "SUMMARY:Pollyanna Brewing Company - Lemont\n" + "DESCRIPTION:\n" +
        "URL:https://smokinbbqkitchen.com/event/pollyanna-brewing-company-lemont-3/\n" +
        "LOCATION:Pollyanna Brewing Company – Lemont\\, 431 Talcott Ave\\, Lemont\\, 60439\\, United States\n" +
        "END:VEVENT\n" + "BEGIN:VEVENT\n" + "DTSTART;TZID=America/Chicago:20181103T170000\n" +
        "DTEND;TZID=America/Chicago:20181103T210000\n" + "DTSTAMP:20181014T193152\n" + "CREATED:20181012T200010Z\n" +
        "LAST-MODIFIED:20181012T200010Z\n" + "UID:83-1541264400-1541278800@smokinbbqkitchen.com\n" +
        "SUMMARY:Impirial Oak Brewing Co\n" + "DESCRIPTION:\n" +
        "URL:https://smokinbbqkitchen.com/event/impirial-oak-brewing-co/\n" +
        "LOCATION:Imperial Oak Brewing Company\\, 501 Willow Blvd\\, Willow Springs\\, IL\\, 60172\\, United States\n" +
        "END:VEVENT\n" + "END:VCALENDAR";
    Truck truck = Truck.builder().id("smokingbbqkitchen").name("BBQ Truck").build();

    when(locator.locateOpt(any())).thenReturn(Optional.of(ModelTestHelper.clarkAndMonroe()));
    List<TempTruckStop> stops = reader.findStops(input, truck.getId());
    assertThat(stops).hasSize(10);


  }
}