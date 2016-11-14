package foodtruck.model;

import java.security.Principal;

import org.joda.time.DateTime;

/**
 * @author aviolette
 * @since 11/14/16
 */
public class User extends ModelEntity implements Principal {
  private String firstName, lastName, email, hashedPassword;
  private DateTime modified, created;

  private User(Builder builder) {
    super(builder.key);
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.hashedPassword = hashedPassword;
    this.modified = modified;
    this.created = created;
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

  public DateTime getModified() {
    return modified;
  }

  public DateTime getCreated() {
    return created;
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
    private String firstName, lastName, email, hashedPassword;
    private DateTime modified = new DateTime(), created = new DateTime();
    private Long key;

    public Builder() {
    }

    public Builder(User instance) {
      this.firstName = instance.firstName;
      this.lastName = instance.lastName;
      this.email = instance.email;
      this.hashedPassword = instance.hashedPassword;
      this.modified = instance.modified;
      this.created = instance.created;
      this.key = (Long) instance.key;
    }

    

    public User build() {
      return new User(this);
    }

  }
}
