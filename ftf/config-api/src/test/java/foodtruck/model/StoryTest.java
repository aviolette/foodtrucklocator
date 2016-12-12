package foodtruck.model;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 1/28/16
 */
public class StoryTest {

  @Test
  public void testGetSanitizedText() throws Exception {
    Story story = Story.builder()
        .text(
            "Come get some dinner with us tonight  @ 400 N. McClurg Court 4:30-9 and get some fresh healthy Lebanese cuisine #halal #vegan #vegetarian")
        .build();
    assertThat(story.getSanitizedText()).isEqualTo(
        "Come get some dinner with us tonight  @ 400 N. McClurg Court 4:30-9 and get some fresh healthy Lebanese cuisine #halal #vegan #vegetarian");
  }
}