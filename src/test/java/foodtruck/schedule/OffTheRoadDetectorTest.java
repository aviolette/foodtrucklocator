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
    assertTrue(topic.offTheRoad("Sorry Wabash & Van Buren!"));
  }

  @Test
  public void offTheRoad_stayingIn() throws Exception {
    assertTrue(topic.offTheRoad("Looks like it's going to be a wet day today. Staying in. See you all tomorrow!"));
  }

  @Test
  public void offTheRoad_mechanical() throws Exception {
    assertTrue(topic.offTheRoad("Encountered a mechanical issue that needs to be addressed ASAP!"));
  }

  @Test
  public void testOffTheRoad_rainCheck() throws Exception {
    assertTrue(topic.offTheRoad("600 W Chicago Ave- we'll have to take a rain check today!!! See you next week...stay dry amigos!"));
  }

  @Test
  public void testOffTheRoad_withOffTheRoad() throws Exception {
    assertTrue(topic.offTheRoad("The @theslideride will be off the road today"));
  }

  @Test
  public void testOffTheRoad_noMarkerText() throws Exception {
    assertFalse(topic.offTheRoad("Heading to Madison and Wacker today. Get out from behind that desk and come see us."));
  }

  @Test
  public void testOffTheRoad_withInTheShop() throws Exception {
    assertTrue(topic.offTheRoad(
        "#cupcake #gangsters os in the shop! Getting our a/c fixed! Give us a call today for #delivery… instagram.com/p/ZS8MbvlUKg/"));
  }

  @Test
  public void testOffTheRoad_maintenance() throws Exception {
    assertTrue(topic.offTheRoad(
        "No stops today because the truck needs maintenance"));
  }
}
