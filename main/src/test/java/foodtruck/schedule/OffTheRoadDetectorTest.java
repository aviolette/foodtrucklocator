package foodtruck.schedule;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 5/15/13
 */
public class OffTheRoadDetectorTest {
  private OffTheRoadDetector topic;

  @Before
  public void before() {
    topic = new OffTheRoadDetector();
  }

  @Test
  public void offTheRoad_sorry() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad("Sorry Wabash & Van Buren!");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isFalse();
  }

  @Test
  public void cancelStop_withDelivery() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad("'Fraid we have to cancel our Ravenswood stop tonight. You can always warm up your night with delivery tho! Call 773.755.5411 min. 10 emps");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isTrue();
  }

  @Test
  public void cancelStop_withoutDelivery() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad("'Fraid we have to cancel our Ravenswood stop tonight. ");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isFalse();
  }

  @Test
  public void offTheRoad_stayingIn() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic
        .offTheRoad("Looks like it's going to be a wet day today. Staying in. See you all tomorrow!");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isFalse();
  }

  @Test
  public void offTheRoadStart() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic
        .offTheRoad("Off the road today for a private event!  After that we have to get a another engine" +
            " put in so we will not be on... fb.me/Vkebi4sv ");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isTrue();
  }

  @Test
  public void inTheShop() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic
        .offTheRoad("The #Cupcake #Truck's #BRAKES FAILED!!! The truck will be in the shop ALL DAY #today?");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isTrue();
  }

  @Test
  public void offTheRoad_mechanical() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic
        .offTheRoad("Encountered a mechanical issue that needs to be addressed ASAP!");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isFalse();
  }

  @Test
  public void testOffTheRoad_rainCheck() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic
        .offTheRoad("600 W Chicago Ave- we'll have to take a rain check today!!! See you next week...stay dry amigos!");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isFalse();
  }

  @Test
  public void testOffTheRoad_withOffTheRoad() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad("The @theslideride will be off the road today");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isTrue();
  }

  @Test
  public void testOffTheRoad_noMarkerText() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic
        .offTheRoad("Heading to Madison and Wacker today. Get out from behind that desk and come see us.");
    assertThat(offTheRoadResponse.isOffTheRoad()).isFalse();
  }

  @Test
  public void testOffTheRoad_withInTheShop() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad(
        "#cupcake #gangsters os in the shop! Getting our a/c fixed! Give us a call today for #delivery… instagram.com/p/ZS8MbvlUKg/");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isTrue();
  }

  @Test
  public void testOffTheRoad_maintenance() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad(
        "The truck needs maintenance");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isFalse();
  }

  @Test
  public void testOffTheRoad_nostops() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad(
        "No stops today because the truck needs maintenance");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isTrue();
  }

  @Test
  public void noServiceToday() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad(
        "No service today due to the inclement weather");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isTrue();
  }

  @Test
  public void atTheShop() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad(
        "The tamale spaceship is at the shop this morning so NO <<Clark & Washington>> today we apologize");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isTrue();
  }

  @Test
  public void noTruckToday() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad(
        "No truck today guys. Repeat, no truck today. We're just as sad but look for us tomorrow at Clark and Monroe then river north at 2pm!");
    assertThat(offTheRoadResponse.isOffTheRoad()).isTrue();
    assertThat(offTheRoadResponse.isConfidenceHigh()).isTrue();
  }
}
