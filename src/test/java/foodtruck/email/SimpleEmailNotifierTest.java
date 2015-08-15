package foodtruck.email;

import com.google.common.collect.ImmutableSet;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.TruckStop;

import static org.easymock.EasyMock.expect;

/**
 * @author aviolette
 * @since 8/13/15
 */
public class SimpleEmailNotifierTest extends EasyMockSupport {

  private SimpleEmailNotifier notifier;
  private StaticConfig config;
  private EmailSender sender;

  @Before
  public void before() {
    DateTimeZone zone = DateTimeZone.forID("America/Chicago");
    DateTimeFormatter timeOnlyFormatter = DateTimeFormat.forPattern("hh:mm a").withZone(zone);
    DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("YYYYMMdd-HHmm").withZone(zone);
    config = createMock(StaticConfig.class);
    sender = createMock(EmailSender.class);
    notifier = new SimpleEmailNotifier(null, timeOnlyFormatter, config, timeFormatter, sender);
  }



  @Test
  public void testNotifyAddMentionedTrucks() throws Exception {
    expect(config.getBaseUrl()).andReturn("http://localhost");
    TruckStop stop = TruckStop.builder()
        .startTime(new DateTime(2015, 8, 13, 12, 0, DateTimeZone.UTC))
        .endTime(new DateTime(2015, 8, 13, 2, 0, DateTimeZone.UTC))
        .location(Location.builder().key(123L).build())
        .build();
    sender.sendSystemMessage("Truck was mentioned by another truck", "This tweet \"foobar\"\n" +
        "\n might have indicated that there additional trucks to be added to the system.\n\n  " +
        "Click here http://localhost/admin/event_at/123?selection=truck1,truck2&startTime=20150813-0700&endTime=20150812-2100 to add the trucks");
    replayAll();
    notifier.notifyAddMentionedTrucks(ImmutableSet.of("truck1", "truck2"), stop, "foobar");
    verifyAll();
  }
}