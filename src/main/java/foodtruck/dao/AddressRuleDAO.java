package foodtruck.dao;

import foodtruck.model.AddressRuleScript;

/**
 * @author aviolette@gmail.com
 * @since 8/19/12
 */
public interface AddressRuleDAO extends DAO<Long, AddressRuleScript> {
  AddressRuleScript findSingleton();
}
