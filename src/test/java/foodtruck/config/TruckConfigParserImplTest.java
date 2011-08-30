package foodtruck.config;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;

import foodtruck.model.ReoccurringTruckStop;
import foodtruck.model.Truck;
import foodtruck.schedule.GoogleCalendarStrategy;
import foodtruck.schedule.ReoccuringScheduleStrategy;
import foodtruck.schedule.ScheduleStrategy;
import static org.junit.Assert.assertEquals;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class TruckConfigParserImplTest extends EasyMockSupport {
  private TruckConfigParserImpl parser;
  private ScheduleStrategy strategy;
  private GoogleCalendarStrategy googleCalendarStrategy;

  @Before
  public void before() {
    strategy = createMock(ScheduleStrategy.class);
    googleCalendarStrategy = createMock(GoogleCalendarStrategy.class);
    final DateTimeZone dateTimeZone = DateTimeZone.forID("America/Chicago");
    parser = new TruckConfigParserImpl(dateTimeZone,
        DateTimeFormat.forPattern("MM/dd/YYYY").withZone(dateTimeZone), null);
  }

  @Test
  public void testLoadSample() throws FileNotFoundException {
    String url =
        Thread.currentThread().getContextClassLoader().getResource("trucks.sample.yaml").getFile();
    replayAll();
    Map<Truck, ScheduleStrategy> result = parser.parse(url, strategy);
    assertEquals(2, result.size());
    // truck 1
    Truck truck =
        new Truck.Builder().id("scheduleTruck").name("Truck that runs on a fixed schedule")
            .twitterHandle("foobar").iconUrl("http://foo/bar.jpg").build();
    ReoccuringScheduleStrategy strategy = (ReoccuringScheduleStrategy) result.get(truck);
    List<ReoccurringTruckStop> stops = strategy.getStops();
    assertEquals(2, stops.size());
    // truck 2
    Truck truck2 =
        new Truck.Builder().id("truck2").name("Truck whose location is determined by twitter")
            .twitterHandle("truck2").iconUrl("https://foo/baz.jpg").build();
    ScheduleStrategy strategy2 = result.get(truck2);
    assertEquals(this.strategy, strategy2);
    verifyAll();
  }

  @Test(expected = FileNotFoundException.class)
  public void testBadFileThrowsException() throws FileNotFoundException {
    replayAll();
    parser.parse("foobar.bar", strategy);
    verifyAll();
  }
}
