package foodtruck.schedule;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    assertTrue(offTheRoadResponse.isOffTheRoad());
    assertFalse(offTheRoadResponse.isConfidenceHigh());
  }

  @Test
  public void cancelStop_withDelivery() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad("'Fraid we have to cancel our Ravenswood stop tonight. You can always warm up your night with delivery tho! Call 773.755.5411 min. 10 emps");
    assertTrue(offTheRoadResponse.isOffTheRoad());
    assertTrue(offTheRoadResponse.isConfidenceHigh());
  }

  @Test
  public void cancelStop_withoutDelivery() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad("'Fraid we have to cancel our Ravenswood stop tonight. ");
    assertTrue(offTheRoadResponse.isOffTheRoad());
    assertFalse(offTheRoadResponse.isConfidenceHigh());
  }

  @Test
  public void offTheRoad_stayingIn() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic
        .offTheRoad("Looks like it's going to be a wet day today. Staying in. See you all tomorrow!");
    assertTrue(offTheRoadResponse.isOffTheRoad());
    assertFalse(offTheRoadResponse.isConfidenceHigh());
  }

  @Test
  public void offTheRoadStart() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic
        .offTheRoad("Off the road today for a private event!  After that we have to get a another engine" +
            " put in so we will not be on... fb.me/Vkebi4sv ");
    assertTrue(offTheRoadResponse.isOffTheRoad());
    assertTrue(offTheRoadResponse.isConfidenceHigh());
  }

  @Test
  public void inTheShop() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic
        .offTheRoad("The #Cupcake #Truck's #BRAKES FAILED!!! The truck will be in the shop ALL DAY #today?");
    assertTrue(offTheRoadResponse.isOffTheRoad());
    assertTrue(offTheRoadResponse.isConfidenceHigh());
  }

  @Test
  public void offTheRoad_mechanical() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic
        .offTheRoad("Encountered a mechanical issue that needs to be addressed ASAP!");
    assertTrue(offTheRoadResponse.isOffTheRoad());
    assertFalse(offTheRoadResponse.isConfidenceHigh());
  }

  @Test
  public void testOffTheRoad_rainCheck() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic
        .offTheRoad("600 W Chicago Ave- we'll have to take a rain check today!!! See you next week...stay dry amigos!");
    assertTrue(offTheRoadResponse.isOffTheRoad());
    assertFalse(offTheRoadResponse.isConfidenceHigh());
  }

  @Test
  public void testOffTheRoad_withOffTheRoad() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad("The @theslideride will be off the road today");
    assertTrue(offTheRoadResponse.isOffTheRoad());
    assertTrue(offTheRoadResponse.isConfidenceHigh());
  }

  @Test
  public void testOffTheRoad_noMarkerText() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic
        .offTheRoad("Heading to Madison and Wacker today. Get out from behind that desk and come see us.");
    assertFalse(offTheRoadResponse.isOffTheRoad());
  }

  @Test
  public void testOffTheRoad_withInTheShop() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad(
        "#cupcake #gangsters os in the shop! Getting our a/c fixed! Give us a call today for #delivery… instagram.com/p/ZS8MbvlUKg/");
    assertTrue(offTheRoadResponse.isOffTheRoad());
    assertTrue(offTheRoadResponse.isConfidenceHigh());
  }

  @Test
  public void testOffTheRoad_maintenance() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad(
        "The truck needs maintenance");
    assertTrue(offTheRoadResponse.isOffTheRoad());
    assertFalse(offTheRoadResponse.isConfidenceHigh());
  }

  @Test
  public void testOffTheRoad_nostops() throws Exception {
    final OffTheRoadResponse offTheRoadResponse = topic.offTheRoad(
        "No stops today because the truck needs maintenance");
    assertTrue(offTheRoadResponse.isOffTheRoad());
    assertTrue(offTheRoadResponse.isConfidenceHigh());
  }
}
