package foodtruck.alexa;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;


/**
 * @author aviolette
 * @since 8/27/16
 */
public class AlexaUtilsTest {

  @Test
  public void toAlexaList_NoVals() {
    assertThat(AlexaUtils.toAlexaList(ImmutableList.<String>of(), false)).isEqualTo("");
  }

  @Test
  public void toAlexaList_OneVal() {
    assertThat(AlexaUtils.toAlexaList(ImmutableList.of("red"), false)).isEqualTo("red");
  }

  @Test
  public void toAlexaList_TwoVal() {
    assertThat(AlexaUtils.toAlexaList(ImmutableList.of("red", "blue"), false)).isEqualTo("red and blue");
  }

  @Test
  public void toAlexaList_ThreeVal() {
    assertThat(AlexaUtils.toAlexaList(ImmutableList.of("red", "blue", "green"), false)).isEqualTo(
        "red, blue, and green");
  }
}