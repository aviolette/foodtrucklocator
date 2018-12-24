package foodtruck.schedule;

import java.util.List;

import foodtruck.model.TempTruckStop;

/**
 * @author aviolette
 * @since 2018-12-18
 */
public interface StopReader {

  List<TempTruckStop> findStops(String document);

  String getCalendar();
}
