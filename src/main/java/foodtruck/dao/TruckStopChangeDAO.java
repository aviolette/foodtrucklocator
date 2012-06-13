package foodtruck.dao;

import java.util.Collection;

import foodtruck.model.TruckStopChange;

/**
 * @author aviolette@gmail.com
 * @since 5/24/12
 */
public interface TruckStopChangeDAO extends DAO<Long, TruckStopChange> {
  void deleteAll(Collection<TruckStopChange> changes);
}
