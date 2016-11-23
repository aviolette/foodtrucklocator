package foodtruck.dao.appengine;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.UserDAO;
import foodtruck.model.User;

import static foodtruck.dao.appengine.Attributes.setDateProperty;

/**
 * @author aviolette
 * @since 11/16/16
 */
public class UserDAOAppEngine extends AppEngineDAO<Long, User> implements UserDAO {
  private static final String EMAIL = "email";
  private static final String FIRST_NAME = "first_name";
  private static final String LAST_NAME = "last_name";
  private static final String LAST_LOGIN = "last_login";
  private static final String HASHED_PASSWORD = "hashed_password";
  private static final String MODIFIED = "modified";
  private static final String CREATED = "created";

  @Inject
  public UserDAOAppEngine(Provider<DatastoreService> provider) {
    super("user", provider);
  }


  @Nullable
  @Override
  public User findByEmail(String email) {
    return aq().filter(predicate(EMAIL, Query.FilterOperator.EQUAL, email))
        .findOne();
  }

  @Override
  protected Entity toEntity(User obj, Entity entity) {
    entity.setProperty(EMAIL, obj.getEmail());
    entity.setProperty(FIRST_NAME, obj.getFirstName());
    entity.setProperty(LAST_NAME, obj.getLastName());
    entity.setProperty(HASHED_PASSWORD, obj.getHashedPassword());
    setDateProperty(LAST_LOGIN, entity, obj.getLastLogin());
    return entity;
  }

  @Override
  protected User fromEntity(Entity entity) {
    FluidEntity fe = new FluidEntity(entity);
    return User.builder()
        .key(fe.longId())
        .email(fe.stringVal(EMAIL))
        .firstName(fe.stringVal(FIRST_NAME))
        .lastName(fe.stringVal(LAST_NAME))
        .hashedPassword(fe.stringVal(HASHED_PASSWORD))
        .lastLogin(fe.dateVal(LAST_LOGIN))
        .build();
  }
}
