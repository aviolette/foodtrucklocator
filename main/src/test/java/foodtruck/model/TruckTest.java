package foodtruck.model;

import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author aviolette
 * @since 8/4/15
 */
public class TruckTest {

  @Test
  public void savoryTest1() {
    Truck truck = Truck.builder().categories(ImmutableSet.of("Lunch, Sandwiches")).build();
    assertTrue(truck.isSavory());
  }

  @Test
  public void savoryTest2() {
    Truck truck = Truck.builder().categories(ImmutableSet.of("Lunch, Sandwiches, Dessert")).build();
    assertTrue(truck.isSavory());
  }

  @Test
  public void savoryTest3() {
    Truck truck = Truck.builder().categories(ImmutableSet.of("Cupcakes")).build();
    assertFalse(truck.isSavory());
  }

  @Test
  public void savoryTest4() {
    Truck truck = Truck.builder().categories(ImmutableSet.of("Dessert")).build();
    assertFalse(truck.isSavory());
  }

  @Test
  public void savoryTest5() {
    Truck truck = Truck.builder().categories(ImmutableSet.of("Dogs")).build();
    assertFalse(truck.isSavory());
  }

  @Test
  public void testPhoneNormalization1() {
    Truck truck = Truck.builder().normalizePhone("847.555.1212").build();
    assertEquals("847-555-1212", truck.getPhone());
  }

  @Test
  public void testPhoneNormalization2() {
    Truck truck = Truck.builder().normalizePhone("(847) 555-1212").build();
    assertEquals("847-555-1212", truck.getPhone());
  }

  @Test
  public void testPhoneNormalization3() {
    Truck truck = Truck.builder().normalizePhone("ABCD123").build();
    assertEquals("ABCD123", truck.getPhone());
  }
}