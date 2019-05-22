package foodtruck.time;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TimeUtilsTest {

  private ZoneId zone;
  private ZonedDateTime four;
  private ZonedDateTime five;
  private ZonedDateTime eight;
  private ZonedDateTime nine;
  private ZonedDateTime one;
  private ZonedDateTime two;

  @Before
  public void before() {
    this.zone = ZoneId.of("America/Chicago");
    this.one = ZonedDateTime.of(2019, 11, 1, 13, 0, 0, 0, zone);
    this.two = ZonedDateTime.of(2019, 11, 1, 14, 0, 0, 0, zone);
    this.four = ZonedDateTime.of(2019, 11, 1, 16, 0, 0, 0, zone);
    this.five = ZonedDateTime.of(2019, 11, 1, 17, 0, 0, 0, zone);
    this.eight = ZonedDateTime.of(2019, 11, 1, 20, 0, 0, 0, zone);
    this.nine = ZonedDateTime.of(2019, 11, 1, 20, 0, 0, 0, zone);

  }

  @Test
  public void overlap_noOverlap() {
    assertThat(TimeUtils.overlap(one, two, four, five)).isFalse();
  }

  @Test
  public void overlap_same() {
    assertThat(TimeUtils.overlap(one, two, one, two)).isTrue();
  }


  @Test
  public void overlap_intersection() {
    assertThat(TimeUtils.overlap(one, five, four, nine)).isTrue();
  }

  @Test
  public void overlap_touch() {
    assertThat(TimeUtils.overlap(one, five, five, nine)).isFalse();
  }

  @Test
  public void overlap_contains() {
    assertThat(TimeUtils.overlap(four, nine, five, eight)).isTrue();
  }

  @Test
  public void overlap_containsSameEnd() {
    assertThat(TimeUtils.overlap(four, eight, five, eight)).isTrue();
  }
}