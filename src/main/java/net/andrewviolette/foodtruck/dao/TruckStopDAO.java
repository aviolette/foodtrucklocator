package net.andrewviolette.foodtruck.dao;

import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import net.andrewviolette.foodtruck.model.TruckStop;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public interface TruckStopDAO {
  Set<TruckStop> findAfter(DateTime instant);

  void deleteAfter(DateTime startTime);

  void addStops(List<TruckStop> stops);
}
