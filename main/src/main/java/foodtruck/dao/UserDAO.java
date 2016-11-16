package foodtruck.dao;

import javax.annotation.Nullable;

import foodtruck.model.User;

/**
 * @author aviolette
 * @since 11/15/16
 */
public interface UserDAO extends DAO<Long, User> {
  @Nullable
  User findByEmail(String email);
}
