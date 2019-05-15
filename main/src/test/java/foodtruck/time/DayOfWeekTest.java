package foodtruck.time;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class DayOfWeekTest {


  @Test
  public void days() {
    assertThat(DayOfWeek.fromConstant(1)).isEqualTo(DayOfWeek.monday);
    assertThat(DayOfWeek.fromConstant(2)).isEqualTo(DayOfWeek.tuesday);
    assertThat(DayOfWeek.fromConstant(7)).isEqualTo(DayOfWeek.sunday);
  }
}