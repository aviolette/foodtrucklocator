package foodtruck.dao;

import java.util.List;

import foodtruck.model.AddressRuleTest;

/**
 * @author aviolette@gmail.com
 * @since 9/4/12
 */
public interface AddressRuleTestDAO extends DAO<Long, AddressRuleTest> {
  void delete(long id);

  List<AddressRuleTest> findAllByName();
}
