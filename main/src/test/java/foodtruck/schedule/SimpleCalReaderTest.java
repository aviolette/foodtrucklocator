package foodtruck.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import com.google.common.io.ByteStreams;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.model.TempTruckStop;
import foodtruck.time.Clock;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 2018-12-11
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleCalReaderTest extends Mockito {

  private @Mock Clock clock;
  private SimpleCalReader reader;

  @Before
  public void setup() {
    when(clock.now8()).thenReturn(ZonedDateTime.of(2018, 12, 11, 9, 0, 0, 0, ZoneId.of("America/Chicago")));
    reader = new SimpleCalReader(clock);
  }

  @Test
  public void read() throws IOException, JSONException {
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream("imperialoak.json");
    JSONArray arr = new JSONArray(new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8));
    List<TempTruckStop> items = reader.read(arr, "imperialoak", "Imperial Oak Brewery");

    assertThat(items).contains(TempTruckStop.builder()
        .calendarName("imperialoak")
        .locationName("Imperial Oak Brewery")
        .startTime(ZonedDateTime.of(2018, 12, 13, 17, 0, 0, 0, ZoneOffset.of("-06:00")))
        .endTime(ZonedDateTime.of(2018, 12, 13, 22, 0, 0, 0, ZoneOffset.of("-06:00")))
        .truckId("bopbartruck")
        .build());
    assertThat(items).hasSize(12);
  }

  @Test
  public void perkNPickle() {
    assertThat(SimpleCalReader.inferTruckId("food truck: perk n pickle")).isEqualTo("perknpickle");
  }
}