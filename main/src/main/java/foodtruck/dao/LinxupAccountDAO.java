package foodtruck.dao;

import java.util.List;

import javax.annotation.Nullable;

import foodtruck.model.LinxupAccount;

/**
 * @author aviolette
 * @since 10/18/16
 */
public interface LinxupAccountDAO extends DAO<Long, LinxupAccount> {
  @Nullable
  LinxupAccount findByTruck(String truckId);

  List<LinxupAccount> findActive();
}
