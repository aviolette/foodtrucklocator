package foodtruck.book;

import javax.annotation.Nullable;

import foodtruck.model.User;

/**
 * @author aviolette
 * @since 11/15/16
 */
public interface UserService {
  /**
   * Creates a user.
   *
   * @param firstname            the first name
   * @param lastName             the last name
   * @param email                the email
   * @param password             the password
   * @param passwordConfirmation the password confirmation
   * @return the created user
   * @throws IllegalArgumentException if the password and password confirmation do not match
   * @throws IllegalStateException    if the user already exists
   */
  User createUser(@Nullable String firstname, @Nullable String lastName, @Nullable String email,
      @Nullable String password,
      @Nullable String passwordConfirmation) throws IllegalArgumentException, IllegalStateException;
}
