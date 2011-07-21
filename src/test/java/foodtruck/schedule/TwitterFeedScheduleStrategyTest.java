package foodtruck.schedule;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.TimeRange;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * @author aviolette@gmail.com
 * @since Jul 15, 2011
 */
public class TwitterFeedScheduleStrategyTest extends EasyMockSupport {
  private TwitterFeedScheduleStrategy strategy;
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
    strategy = new TwitterFeedScheduleStrategy(twitterWrapper, locator);
  }

  // TODO: include geolocation data
  private Status createStatusMock(boolean reply, boolean retweet, String text, DateTime dateTime) {
    Status status = createMock(Status.class);
    expect(status.getInReplyToUserId()).andStubReturn(reply ? 256L : -1L);
    expect(status.isRetweet()).andStubReturn(retweet);
    expect(status.getText()).andStubReturn(text);
    expect(status.getCreatedAt()).andStubReturn(dateTime.toDate());
    return status;
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
