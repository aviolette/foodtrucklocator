package foodtruck.datalake;

import java.io.IOException;

/**
 * @author aviolette
 * @since 2018-12-29
 */
public interface DataExportService {

  void exportTrucks(String bucket) throws IOException;

}
