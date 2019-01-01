package foodtruck.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.io.ByteStreams;

import foodtruck.model.TempTruckStop;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 2018-12-30
 */
class AbstractReaderTest<T extends StopReader> {

  private final Supplier<T> supplier;

  AbstractReaderTest(Supplier<T> readerSupplier) {
    this.supplier = readerSupplier;
  }

  List<TempTruckStop> execFindStop(String file) throws IOException {
    InputStream str = ClassLoader.getSystemClassLoader()
        .getResourceAsStream(file);
    String doc = new String(ByteStreams.toByteArray(str), StandardCharsets.UTF_8);

    return supplier.get().findStops(doc);
  }
}
