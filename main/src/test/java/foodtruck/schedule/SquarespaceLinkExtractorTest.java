package foodtruck.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.common.io.ByteStreams;

import org.junit.Before;
import org.junit.Test;

import foodtruck.model.Truck;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.schedule.ModelTestHelper.truck1;

/**
 * @author aviolette
 * @since 10/16/18
 */
public class SquarespaceLinkExtractorTest {

  private SquarespaceLinkExtractor extractor;

  @Before
  public void setup() {
    extractor = new SquarespaceLinkExtractor();
  }

  @Test
  public void findLinks() throws IOException {
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("bigwangs.html");
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);

    List<String> links = extractor.findLinks(doc, "https://www.bigwangsfoodtruck.com/events");
    assertThat(links).hasSize(21);
    assertThat(links).contains(
        "https://www.bigwangsfoodtruck.com/events/2018/8/1/u-of-c-hyde-park-57th-s-ellis-lunch-service-jtexp?format=ical");
  }
}