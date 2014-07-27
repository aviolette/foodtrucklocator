package foodtruck.model;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author aviolette
 * @since 7/29/14
 */
public class PetitionSignature extends ModelEntity {
  private final String petitionId;
  private final DateTime created;
  private final @Nullable DateTime signed;
  private final String signature;
  private final String name;
  private final String email;
  private final String zipcode;

  public PetitionSignature(Builder builder) {
    super(builder.key);
    this.petitionId = builder.petitionId;
    this.created = builder.created;
    this.signed = builder.signed;
    this.signature = builder.signature;
    this.name = builder.name;
    this.email = builder.email;
    this.zipcode = builder.zipcode;
  }

  public String getPetitionId() {
    return petitionId;
  }

  public DateTime getCreated() {
    return created;
  }

  public DateTime getSigned() {
    return signed;
  }

  public String getSignature() {
    return signature;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getZipcode() {
    return zipcode;
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("petition id", petitionId)
        .add("created", created)
        .add("signed", signed)
        .add("signature", signature)
        .add("name", name)
        .add("email", email)
        .add("zipcode", zipcode)
        .toString();
  }

  public static Builder builder(PetitionSignature sig) {
    return new Builder(sig);
  }

  @Override public void validate() throws IllegalStateException {
    super.validate();
    checkState(!Strings.isNullOrEmpty(name), "Name is not specified");
    checkState(!Strings.isNullOrEmpty(petitionId), "Petition ID is not specified");
    checkState(!Strings.isNullOrEmpty(signature), "Signature is not specified");
    checkState(!Strings.isNullOrEmpty(email), "Email is not specified");
    checkState(!Strings.isNullOrEmpty(zipcode), "Zip code is not specified");
  }

  public static class Builder {
    private @Nullable Object key;
    private String petitionId;
    private DateTime created;
    private @Nullable DateTime signed;
    private String signature;
    private String name;
    private String email;
    private String zipcode;

    public Builder() {}

    public Builder(PetitionSignature sig) {
      petitionId = sig.petitionId;
      created = sig.created;
      signature = sig.signature;
      signed = sig.signed;
      name = sig.name;
      email = sig.email;
      key = sig.key;
      zipcode = sig.zipcode;
    }

    public Builder zipcode(String zipcode) {
      this.zipcode = zipcode;
      return this;
    }

    public Builder key(@Nullable Object key) {
      this.key = key;
      return this;
    }

    public Builder petitionId(String petitionId) {
      this.petitionId = petitionId;
      return this;
    }

    public Builder created(DateTime created) {
      this.created = created;
      return this;
    }

    public Builder signature(String signature) {
      this.signature = signature;
      return this;
    }

    public Builder signed(DateTime signed) {
      this.signed = signed;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public PetitionSignature build() {
      return new PetitionSignature(this);
    }
  }
}
