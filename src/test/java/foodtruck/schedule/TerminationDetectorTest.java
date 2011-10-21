package foodtruck.schedule;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import foodtruck.model.TweetSummary;
import foodtruck.util.Clock;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @author aviolette@gmail.com
 * @since 10/20/11
 */
public class TerminationDetectorTest extends EasyMockSupport {
  private DateTime now;
  private Clock clock;
  private TerminationDetector detector;
  private TweetSummary.Builder tweetBuilder;

  @Before
  public void before() {
    now = new DateTime(2011, 10, 10, 9, 8, 7, 0, DateTimeZone.UTC);
    clock = createMock(Clock.class);
    expect(clock.now()).andStubReturn(now);
    detector = new TerminationDetector(clock);
    tweetBuilder = new TweetSummary.Builder();
  }

  @Test
  public void test1() {
    replayAll();
    assertEquals(now, detector.detect(tweetBuilder.text("Thank you U of Chicago for braving the weather today and South Loop for closing out our day!  Enjoy the rest of your night!").build()));
    verifyAll();
  }
}
