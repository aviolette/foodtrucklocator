package foodtruck.notifications;

import java.util.List;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 12/6/12
 */
public class TruckStopSplitterTest {

  @Test
  public void splitLessThan140() {
    String foo = "This is foo bar @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234";
    List<String> notifications = new TruckStopSplitter(null).split(foo);
    assertThat(notifications).hasSize(1);
    assertThat(notifications).contains(foo);
  }

  @Test
  public void splitExactly140() {
    String foo = "0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456789 0123456";
    List<String> notifications = new TruckStopSplitter(null).split(foo);
    assertThat(notifications).hasSize(1);
    assertThat(notifications).contains(foo);
  }

  @Test
  public void splitLessGreater140() {
    String foo =
"Trucks at 600W @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abc1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @splitsonthis @abc1234 " +
        "@abcd1234 @abcd1234";
    List<String> notifications = new TruckStopSplitter("600 West").split(foo);
    assertThat(notifications).hasSize(2);
    assertThat(notifications).containsExactly("Additional trucks at 600 West: @splitsonthis @abc1234 @abcd1234 @abcd1234", "Trucks at 600W @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abc1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234 @abcd1234");
  }
}
