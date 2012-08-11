package foodtruck.model;

import javax.annotation.Nullable;

/**
 * @author aviolette@gmail.com
 * @since 8/19/12
 */
public class AddressRule extends ModelEntity {
  private String pattern;

  public AddressRule(Builder builder) {
    super(builder.key);
    this.pattern = builder.pattern;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getPattern() {
    return pattern;
  }

  public static class Builder {
    private String pattern;
    private Long key;

    public Builder() {}

    public Builder pattern(String pattern) {
      this.pattern = pattern;
      return this;
    }

    public Builder key(@Nullable Long key) {
      this.key = key;
      return this;
    }

    public AddressRule build() {
      return new AddressRule(this);
    }
  }
}
