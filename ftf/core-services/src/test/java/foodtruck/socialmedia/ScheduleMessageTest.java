package foodtruck.socialmedia;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 11/9/16
 */
public class ScheduleMessageTest {

  @Test
  public void getTwitterMessages() throws Exception {
    ScheduleMessage message = new ScheduleMessage(
        "Today's Schedule\n10:00 AM Clark and Monroe\n11:00 Clark and Farragut\n12:00 PM Wacker and Adams\n13:00 600 West Chicago Avenue, Chicago, IL\n2:00 PM Willis Tower\n3:00 PM Empirical Brewery\n5:00 PM Skeleton Key Brewery");
    assertThat(message.getTwitterMessages()).containsExactly("Today's Schedule\n" +
        "10:00 AM Clark and Monroe\n" +
        "11:00 Clark and Farragut\n" +
        "12:00 PM Wacker and Adams\n" +
        "13:00 600 West Chicago Avenue, Chicago, IL\n", "2:00 PM Willis Tower\n" +
        "3:00 PM Empirical Brewery\n" +
        "5:00 PM Skeleton Key Brewery");
  }

  @Test
  public void getTwitterMessages2() throws Exception {
    ScheduleMessage message = new ScheduleMessage(
        "Today's Schedule\n123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
    assertThat(message.getTwitterMessages()).containsExactly(
        "Today's Schedule\n123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123",
        "456789012345678901234567890");
  }

  @Test
  public void getTwitterMessages3() throws Exception {
    String scheduleMessage = "Today's Schedule\n10:00 AM Clark and Monroe\n11:00 Clark and Farragut\n12:00 PM Wacker and Adams\n13:00 600 West Chicago Avenue, Chicago, IL";
    ScheduleMessage message = new ScheduleMessage(scheduleMessage);
    assertThat(message.getTwitterMessages()).containsExactly(message.getFullSchedule());
  }

}