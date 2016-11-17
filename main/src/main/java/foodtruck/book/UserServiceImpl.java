package foodtruck.book;

import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import foodtruck.dao.UserDAO;
import foodtruck.model.User;

/**
 * @author aviolette
 * @since 11/15/16
 */
class UserServiceImpl implements UserService {
  private static final Logger log = Logger.getLogger(UserServiceImpl.class.getName());
  private final UserDAO userDAO;
  private final PasswordHasher hasher;

  @Inject
  public UserServiceImpl(UserDAO userDAO, PasswordHasher passwordHasher) {
    this.userDAO = userDAO;
    this.hasher = passwordHasher;
  }

  @Override
  public User createUser(User user, String confirmation) throws IllegalArgumentException, IllegalStateException {
    user.validate();
    User existing = userDAO.findByEmail(user.getEmail());
    if (existing != null) {
      throw new IllegalArgumentException("User with that email already exists");
    }
    if (Strings.isNullOrEmpty(user.getHashedPassword())) {
      throw new IllegalStateException("Password is not specified");
    }
    if (!user.getHashedPassword()
        .equals(confirmation)) {
      throw new IllegalStateException("Passwords do not match");
    }
    return User.builder(user)
        .key(userDAO.save(user))
        .build();
  }

  @Override
  @Nullable
  public User verifyLogin(String email, String password) {
    User user = userDAO.findByEmail(email);
    if (user == null || !user.hasPassword()) {
      return null;
    }
    return hasher.hash(password)
        .equals(user.getHashedPassword()) ? user : null;
  }
}
