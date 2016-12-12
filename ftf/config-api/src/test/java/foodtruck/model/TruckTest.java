package foodtruck.model;

import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 8/4/15
 */
public class TruckTest {

  @Test
  public void savoryTest1() {
    Truck truck = Truck.builder().categories(ImmutableSet.of("Lunch, Sandwiches")).build();
    assertThat(truck.isSavory()).isTrue();
  }

  @Test
  public void savoryTest2() {
    Truck truck = Truck.builder().categories(ImmutableSet.of("Lunch, Sandwiches, Dessert")).build();
    assertThat(truck.isSavory()).isTrue();
  }

  @Test
  public void savoryTest3() {
    Truck truck = Truck.builder().categories(ImmutableSet.of("Cupcakes")).build();
    assertThat(truck.isSavory()).isFalse();
  }

  @Test
  public void savoryTest4() {
    Truck truck = Truck.builder().categories(ImmutableSet.of("Dessert")).build();
    assertThat(truck.isSavory()).isFalse();
  }

  @Test
  public void savoryTest5() {
    Truck truck = Truck.builder().categories(ImmutableSet.of("Dogs")).build();
    assertThat(truck.isSavory()).isFalse();
  }

  @Test
  public void testPhoneNormalization1() {
    Truck truck = Truck.builder().normalizePhone("847.555.1212").build();
    assertThat(truck.getPhone()).isEqualTo("847-555-1212");
  }

  @Test
  public void testPhoneNormalization2() {
    Truck truck = Truck.builder().normalizePhone("(847) 555-1212").build();
    assertThat(truck.getPhone()).isEqualTo("847-555-1212");
  }

  @Test
  public void testPhoneNormalization3() {
    Truck truck = Truck.builder().normalizePhone("ABCD123").build();
    assertThat(truck.getPhone()).isEqualTo("ABCD123");
  }
}