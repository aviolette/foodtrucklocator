package foodtruck.model;

import java.security.Principal;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

/**
 * @author aviolette
 * @since 11/14/16
 */
public class User extends ModelEntity implements Principal {
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
    return null;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(User instance) {
    return new Builder(instance);
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
