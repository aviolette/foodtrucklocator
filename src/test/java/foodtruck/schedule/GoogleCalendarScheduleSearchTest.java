package foodtruck.schedule;

import java.util.Map;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;
import foodtruck.model.TimeRange;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author aviolette@gmail.com
 * @since 9/19/11
 */
public class GoogleCalendarScheduleSearchTest extends EasyMockSupport {
  private CalendarService calendarService;
  private CalendarQueryFactory calendarFactory;
  private DateTimeZone zone;
  private GeoLocator locator;
  private Map<String, Truck> trucks;
  private GoogleCalendarScheduleSearch searcher;
  private static final String FOO_TRUCK_ID = "foo";
  private Location location;
  private String address;
  private Truck fooTruck;

  @Before
  public void before() {
    calendarService = createMock(CalendarService.class);
    calendarFactory = createMock(CalendarQueryFactory.class);
    zone = DateTimeZone.forID("America/Chicago");
    locator = createMock(GeoLocator.class);
    fooTruck = new Truck.Builder().id(FOO_TRUCK_ID).build();
    trucks = ImmutableMap.of(FOO_TRUCK_ID, fooTruck,
        "bar", new Truck.Builder().id("bar").build());
    searcher = new GoogleCalendarScheduleSearch(calendarService, calendarFactory, zone, locator,
        trucks);
    address = "200 S. Wacker Dr., Chicago, IL";
    location = new Location(-23, -45, "200 S. Wacker Dr., Chicago, IL");
  }

  @Test
  public void parseCalendarEntry_shouldParseValidCalendarEntry() {
    final LocalDate startDate = new LocalDate(2011, 11, 7);
    final TimeRange range = new TimeRange(startDate);
    final CalendarEventEntry entry = createMock(CalendarEventEntry.class);
    expect(entry.getTitle()).andReturn(createPlainTextConstruct(FOO_TRUCK_ID));
    final Where where = new Where("blah", "blah", address);
    expect(entry.getLocations()).andReturn(ImmutableList.<Where>of(where));
    final When when = new When();
    final DateTime st = startDate.toDateMidnight().toDateTime().plusHours(11);
    final DateTime et = st.plusHours(3);
    final com.google.gdata.data.DateTime startTime = new com.google.gdata.data.DateTime(st.getMillis());
    when.setStartTime(startTime);
    final com.google.gdata.data.DateTime endTime = new com.google.gdata.data.DateTime(et.getMillis());
    when.setEndTime(endTime);
    expect(entry.getTimes()).andReturn(
        ImmutableList.<com.google.gdata.data.extensions.When>of(when));
    expect(locator.locate(address)).andReturn(location);
    replayAll();
    TruckStop stop = searcher.parseCalendarEntry(range, entry);
    assertEquals(stop.getEndTime(), et);
    assertEquals(stop.getStartTime(), st);
    assertEquals(stop.getLocation(), location);
    assertEquals(stop.getTruck(), fooTruck);
    verifyAll();
  }

  @Test
  public void parseCalendarEntry_shouldThrowExceptionWhenLocationNotSpecified() {
    final LocalDate startDate = new LocalDate(2011, 11, 7);
    final TimeRange range = new TimeRange(startDate);
    final CalendarEventEntry entry = createMock(CalendarEventEntry.class);
    expect(entry.getTitle()).andReturn(createPlainTextConstruct(FOO_TRUCK_ID));
    expect(entry.getLocations()).andReturn(ImmutableList.<Where>of());
    replayAll();
    try {
      searcher.parseCalendarEntry(range, entry);
    } catch (IllegalStateException ise) {
      assertEquals("No location specified for truck foo", ise.getMessage());
      verifyAll();
      return;
    }
    fail("Exception never thrown");
  }

  @Test
  public void parseCalendarEntry_shouldThrowExceptionWhenLocationCannotBeResolved() {
    final LocalDate startDate = new LocalDate(2011, 11, 7);
    final TimeRange range = new TimeRange(startDate);
    final CalendarEventEntry entry = createMock(CalendarEventEntry.class);
    expect(entry.getTitle()).andReturn(createPlainTextConstruct(FOO_TRUCK_ID));
    final Where where = new Where("blah", "blah", address);
    expect(entry.getLocations()).andReturn(ImmutableList.<Where>of(where));
    expect(locator.locate(address)).andReturn(null);
    replayAll();
    try {
      searcher.parseCalendarEntry(range, entry);
    } catch (IllegalStateException ise) {
      assertEquals("Location could not be resolved for foo, 200 S. Wacker Dr., " +
          "Chicago, IL between 2011-11-07T00:00:00.000-06:00 and 2011-11-07T23:59:00.000-06:00",
          ise.getMessage());
      verifyAll();
      return;
    }
    fail("Exception never thrown");
  }

  @Test
  public void parseCalendarEntry_unknownTruckShouldThrowException() {
    final LocalDate startDate = new LocalDate(2011, 11, 7);
    final TimeRange range = new TimeRange(startDate);
    final CalendarEventEntry entry = createMock(CalendarEventEntry.class);
    expect(entry.getTitle()).andReturn(createPlainTextConstruct("ffffff")).anyTimes();
    replayAll();
    try {
      searcher.parseCalendarEntry(range, entry);
    } catch (IllegalStateException ise) {
      assertEquals("Truck with ID: ffffff could not be resolved", ise.getMessage());
      verifyAll();
      return;
    }
    fail("Exception never thrown");
  }

  private TextConstruct createPlainTextConstruct(String text) {
    TextConstruct construct =  createMock(TextConstruct.class);
    expect(construct.getPlainText()).andReturn(text).anyTimes();
    return construct;
  }
}
