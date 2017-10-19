package foodtruck.model;

import java.io.Serializable;
import java.security.Principal;

import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.joda.time.DateTime;

/**
 * @author aviolette
 * @since 11/14/16
 */
public class User extends ModelEntity implements Principal, Serializable {
  private String firstName, lastName, email;
  private @Nullable String hashedPassword;
  private @Nullable DateTime lastLogin;

  private User(Builder builder) {
    super(builder.key);
    this.firstName = builder.firstName;
    this.lastName = builder.lastName;
    this.email = builder.email;
    this.hashedPassword = builder.hashedPassword;
    this.lastLogin = builder.lastLogin;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(User instance) {
    return new Builder(instance);
  }

  @Override
  public void validate() throws IllegalStateException {
    Preconditions.checkState(!Strings.isNullOrEmpty(email), "Email is not specified");
    Preconditions.checkState(!Strings.isNullOrEmpty(firstName), "First name is not specified");
    Preconditions.checkState(!Strings.isNullOrEmpty(lastName), "Last name is not specified");
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("first name", firstName)
        .add("last name", lastName)
        .add("email", email)
        .add("hashed password", Strings.isNullOrEmpty(hashedPassword) ? "EMPTY" : "EXISTS")
        .add("last login", lastLogin)
        .toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(lastLogin, firstName, lastName, email, hashedPassword, lastLogin);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof User)) {
      return false;
    }
    User u = (User) obj;
    return u.lastName.equals(lastName) && u.firstName.equals(firstName) && u.email.equals(email) && Objects.equal(
        u.hashedPassword, hashedPassword) && Objects.equal(u.lastLogin, lastLogin);
  }

  @Nullable
  public DateTime getLastLogin() {
    return lastLogin;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getHashedPassword() {
    return hashedPassword;
  }

  @Override
  public String getName() {
    // need this to be email so that it is interoperable with vendor dashboard
    return email;
  }

  public boolean hasPassword() {
    return !Strings.isNullOrEmpty(hashedPassword);
  }

  public static class Builder {
    private String firstName, lastName, email;
    private @Nullable String hashedPassword;
    private @Nullable DateTime lastLogin;
    private Long key;

    public Builder() {
    }

    public Builder(User instance) {
      this.firstName = instance.firstName;
      this.lastName = instance.lastName;
      this.email = instance.email;
      this.hashedPassword = instance.hashedPassword;
      this.lastLogin = instance.lastLogin;
      this.key = (Long) instance.key;
    }

    public Builder lastLogin(@Nullable DateTime lastLogin) {
      this.lastLogin = lastLogin;
      return this;
    }

    public Builder key(Long key) {
      this.key = key;
      return this;
    }

    public Builder hashedPassword(String hashedPassword) {
      this.hashedPassword = hashedPassword;
      return this;
    }

    public Builder firstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder lastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public User build() {
      return new User(this);
    }

  }
}
