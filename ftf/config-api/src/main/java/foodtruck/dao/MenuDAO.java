package foodtruck.dao;

import javax.annotation.Nullable;

import foodtruck.model.Menu;

/**
 * @author aviolette
 * @since 8/18/16
 */
public interface MenuDAO extends DAO<Long, Menu> {
  @Nullable
  Menu findByTruck(String truckId);
}
