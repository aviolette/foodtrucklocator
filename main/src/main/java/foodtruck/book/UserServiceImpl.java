package foodtruck.book;

import javax.annotation.Nullable;

import foodtruck.model.User;

/**
 * @author aviolette
 * @since 11/15/16
 */
public class UserServiceImpl implements UserService {
  @Override
  public User createUser(@Nullable String firstname, @Nullable String lastName, @Nullable String email,
      @Nullable String password,
      @Nullable String passwordConfirmation) throws IllegalArgumentException, IllegalStateException {
    return null;
  }
}
