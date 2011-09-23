package foodtruck.schedule;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Truck;
import static org.easymock.EasyMock.expect;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * @author aviolette@gmail.com
 * @since Jul 15, 2011
 */
public class TwitterFeedSearchTest extends EasyMockSupport {
  private TwitterFeedSearch strategy;
  private TwitterFactoryWrapper twitterWrapper;
  private Twitter twitter;
  private Truck truck;
  private GeoLocator locator;

  @Before
  public void before() {
    twitterWrapper = createMock(TwitterFactoryWrapper.class);
    twitter = createMock(Twitter.class);
    truck = new Truck.Builder().id("foobar").twitterHandle("foobar").name("FOobar Truck").build();
    expect(twitterWrapper.create()).andStubReturn(twitter);
    locator = createMock(GeoLocator.class);
  }



  @Test
  public void testFindForTime() throws TwitterException {
/*
    List<Status> statuses =
        ImmutableList.of(createStatusMock(true, false, "@foobar hello world", new DateTime(2011, 7, 11, 23, 0, 0, 0)),
            createStatusMock(false, true, "blah blah blah", new DateTime(2011, 7, 11, 22, 0, 0, 0)),
            createStatusMock(false, false, "Sold out!", new DateTime(2011, 7, 11, 21, 30, 0, 0)),
            createStatusMock(false, false, "We are at Dearborn and Monroe.  Come and get 'em", new DateTime(2011, 7, 11, 21, 00, 0, 0)));

    expect(twitter.getUserTimeline("foobar")).andReturn(new MockResponseList<Status>(statuses));
    replayAll();
    List<TruckStop> stops = strategy.findForTime(truck,
        new TimeRange(new LocalDate(2011, 7, 11), new LocalTime(11, 0), new LocalTime(23, 59)));

    assertEquals(1, stops.size());
    TruckStop stop = stops.get(0);
    assertEquals(truck, stop.getTruck());
    */
  }

}
