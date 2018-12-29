package foodtruck.datalake;

import java.io.IOException;

/**
 * @author aviolette
 * @since 2018-12-29
 */
public interface DataExportService {

  void exportTrucks() throws IOException;

  void exportStopsForMonth(int year, int month) throws IOException;
}
