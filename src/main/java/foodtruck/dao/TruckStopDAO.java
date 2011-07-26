package foodtruck.dao;

import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import foodtruck.model.TruckStop;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public interface TruckStopDAO {
  Set<TruckStop> findAt(DateTime instant);

  void deleteAfter(DateTime startTime);

  void addStops(List<TruckStop> stops);
}
