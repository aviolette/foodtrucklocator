package foodtruck.book;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.dao.UserDAO;
import foodtruck.model.User;
import foodtruck.util.Clock;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * @author aviolette
 * @since 11/16/16
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {
  private @Mock UserDAO userDAO;
  private @Mock User user;
  private @Mock PasswordHasher passwordHasher;
  private @Mock Clock clock;
  private UserServiceImpl userService;

  @Before
  public void before() {
    userService = new UserServiceImpl(userDAO, passwordHasher, clock);
  }

  @Test
  public void createUser() {
    final String email = "foobar@gmail.com", password = "foo";
    when(user.getEmail()).thenReturn(email);
    when(user.getHashedPassword()).thenReturn(password);
    when(userDAO.findByEmail(email)).thenReturn(null);
    userService.createUser(user, password);
    Mockito.verify(user)
        .validate();
    Mockito.verify(userDAO)
        .save(user);
  }

  @Test
  public void createUser_differentPassword() {
    final String email = "foobar@gmail.com", password = "foo";
    when(user.getEmail()).thenReturn(email);
    when(user.getHashedPassword()).thenReturn(password);
    when(userDAO.findByEmail(email)).thenReturn(null);
    try {
      userService.createUser(user, "foobar");
    } catch (IllegalStateException ise) {
      assertThat(ise.getMessage()).isEqualTo("Passwords do not match");
      Mockito.verify(user)
          .validate();
      return;
    }
    fail("Exception never thrown");
  }

  @Test
  public void createUser_noPassword() {
    final String email = "foobar@gmail.com";
    when(user.getEmail()).thenReturn(email);
    when(user.getHashedPassword()).thenReturn(null);
    when(userDAO.findByEmail(email)).thenReturn(null);
    try {
      userService.createUser(user, "foobar");
    } catch (IllegalStateException ise) {
      assertThat(ise.getMessage()).isEqualTo("Password is not specified");
      Mockito.verify(user)
          .validate();
      return;
    }
    fail("Exception never thrown");
  }

  @Test
  public void verifyLogin() {
    final String email = "foobar@gmail.com";
    user = User.builder()
        .email(email)
        .hashedPassword("hashed")
        .firstName("first")
        .lastName("last")
        .build();
    when(userDAO.findByEmail(email)).thenReturn(user);
    when(passwordHasher.hash("unhashed")).thenReturn("hashed");
    DateTime date = new DateTime(2016, 11, 10, 9, 8, 7);
    when(clock.now()).thenReturn(date);
    User modifiedUser = User.builder(user)
        .lastLogin(date)
        .build();
    assertThat(userService.login(email, "unhashed")).isEqualTo(modifiedUser);
    Mockito.verify(userDAO)
        .findByEmail(email);
    Mockito.verify(userDAO)
        .save(modifiedUser);

  }

  @Test
  public void verifyLogin_noMatch() {
    final String email = "foobar@gmail.com";
    when(user.getEmail()).thenReturn(email);
    when(user.getHashedPassword()).thenReturn("hashed");
    when(user.hasPassword()).thenReturn(true);
    when(userDAO.findByEmail(email)).thenReturn(user);
    when(passwordHasher.hash("unhashed")).thenReturn("hashed1");
    assertThat(userService.login(email, "unhashed")).isNull();
    Mockito.verify(userDAO)
        .findByEmail(email);
  }

  @Test
  public void verifyLogin_noPassword() {
    final String email = "foobar@gmail.com";
    when(user.getEmail()).thenReturn(email);
    when(user.hasPassword()).thenReturn(false);
    when(userDAO.findByEmail(email)).thenReturn(user);
    assertThat(userService.login(email, "unhashed")).isNull();
    Mockito.verify(userDAO)
        .findByEmail(email);
  }

  @Test
  public void verifyLogin_noUser() {
    final String email = "foobar@gmail.com";
    when(userDAO.findByEmail(email)).thenReturn(null);
    assertThat(userService.login(email, "unhashed")).isNull();
    Mockito.verify(userDAO)
        .findByEmail(email);
  }
}