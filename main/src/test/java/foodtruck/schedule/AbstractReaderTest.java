package foodtruck.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.io.ByteStreams;

import foodtruck.model.TempTruckStop;

/**
 * @author aviolette
 * @since 2018-12-30
 */
class AbstractReaderTest<T extends StopReader> {
  protected final static ZoneId CHICAGO = ZoneId.of("America/Chicago");
  protected  Supplier<T> supplier;

  AbstractReaderTest() {}

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
