package foodtruck.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.List;

import com.google.common.io.ByteStreams;

import org.junit.Before;
import org.junit.Test;

import foodtruck.model.TempTruckStop;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 2018-12-18
 */
public class SkeletonKeyReaderTest {

  private SkeletonKeyReader reader;

  @Before
  public void before() {
    reader = new SkeletonKeyReader(ZoneId.of("America/Chicago"));
  }

  @Test
  public void findStops() throws IOException {
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("skeleton.html");
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);

    List<TempTruckStop> stops = reader.findStops(doc);
    assertThat(stops).hasSize(5);
  }
}