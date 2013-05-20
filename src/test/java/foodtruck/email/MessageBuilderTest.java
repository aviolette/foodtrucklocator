package foodtruck.email;

import org.junit.Before;
import org.junit.Test;

import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TweetSummary;
import static org.junit.Assert.assertEquals;

/**
 * @author aviolette
 * @since 5/20/13
 */
public class MessageBuilderTest {
  private MessageBuilder topic;

  @Before
  public void before() {
    topic = new MessageBuilder();
  }

  @Test
  public void locationAdded() {
    Location location = Location.builder().name("Foo Bar").key(12345678L).build();
    TweetSummary tweet = TweetSummary.builder().text("You are about to witness the strength of street knowledge")
        .userId("foobar").build();
    Truck truck = Truck.builder().name("The Foobar Truck").id("foobartruck").build();
    assertEquals("This tweet \"You are about to witness the strength of street knowledge\" triggered the " +
        "following location to be added Foo Bar.  Click here to view the " +
        "location http://www.chicagofoodtruckfinder.com/admin/locations/12345678 .  " +
        "Also, view the truck here: http://www.chicagofoodtruckfinder.com/admin/trucks/foobartruck",
        topic.locationAddedMessage(location, tweet, truck));
  }
}
