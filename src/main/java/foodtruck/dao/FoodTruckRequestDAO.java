package foodtruck.dao;

import foodtruck.model.FoodTruckRequest;

/**
 * @author aviolette
 * @since 12/24/13
 */
public interface FoodTruckRequestDAO extends DAO<Long, FoodTruckRequest> {
  Iterable<FoodTruckRequest> findAllForUser(String userId);
}
