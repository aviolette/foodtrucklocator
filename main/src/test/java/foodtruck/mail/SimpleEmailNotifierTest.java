package foodtruck.mail;

import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;

/**
 * @author aviolette
 * @since 8/13/15
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleEmailNotifierTest {

  private SimpleEmailNotifier notifier;
  @Mock private EmailSender sender;

  @Before
  public void before() {
    DateTimeZone zone = DateTimeZone.forID("America/Chicago");
    DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("YYYYMMdd-HHmm")
        .withZone(zone);
    notifier = new SimpleEmailNotifier(timeFormatter, sender, "http://localhost", "Chicago, IL");
  }

  @Test
  public void testNotifyAddMentionedTrucks() throws Exception {
    TruckStop stop = TruckStop.builder()
        .truck(Truck.builder()
            .id("foo")
            .name("bar")
            .build())
        .startTime(new DateTime(2015, 8, 13, 12, 0, DateTimeZone.UTC))
        .endTime(new DateTime(2015, 8, 13, 2, 0, DateTimeZone.UTC))
        .location(Location.builder()
            .key(123L)
            .build())
        .build();
    sender.sendSystemMessage("Truck was mentioned by another truck", "This tweet \"foobar\"\n" +
        "\n from bar might have indicated that there additional trucks to be added to the system.\n\n  " +
        "Click here http://localhost/admin/event_at/123?selected=truck1,truck2&startTime=20150813-0700&endTime=20150812-2100 to add the trucks");
    notifier.notifyAddMentionedTrucks(ImmutableSet.of("truck1", "truck2"), stop, "foobar");
  }
}