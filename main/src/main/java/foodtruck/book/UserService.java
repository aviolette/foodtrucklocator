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
   * @throws IllegalArgumentException if the password and password confirmation do not match
   * @throws IllegalStateException    if the user already exists
   */
  User createUser(User user, String confirmation) throws IllegalArgumentException, IllegalStateException;

  /**
   * Verifies a user exists and that the password (when hashed) matches the stored password.
   *
   * @param email    the email
   * @param password the password (plain-text)
   * @return the user object if everything is verified, null otherwise.
   */
  @Nullable
  User login(String email, String password);
}
