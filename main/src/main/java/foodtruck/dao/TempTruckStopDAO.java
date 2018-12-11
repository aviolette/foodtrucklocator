package foodtruck.dao;

import java.util.List;

import javax.annotation.Nullable;

import org.joda.time.Interval;

import foodtruck.model.TempTruckStop;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 2018-12-10
 */
public interface TempTruckStopDAO extends DAO<Long, TempTruckStop> {

  List<TempTruckStop> findDuring(Interval range, @Nullable Truck searchTruck);

  void deleteAll();
}
