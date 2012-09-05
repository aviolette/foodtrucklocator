package foodtruck.model;

/**
 * @author aviolette@gmail.com
 * @since 9/4/12
 */
public class AddressRuleTest extends ModelEntity {
  private final String name;
  private final String truck;
  private final String input;
  private final String expected;

  public AddressRuleTest(Builder builder) {
    super(builder.key);
    this.name = builder.name;
    this.truck = builder.truck;
    this.input = builder.input;
    this.expected = builder.expected;
  }

  public String getName() {
    return name;
  }

  public String getTruck() {
    return truck;
  }

  public String getInput() {
    return input;
  }

  public String getExpected() {
    return expected;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private String truck;
    private String input;
    private String expected;
    private Long key;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder truck(String truckId) {
      this.truck = truckId;
      return this;
    }

    public Builder input(String input) {
      this.input = input;
      return this;
    }

    public Builder expected(String expected) {
      this.expected = expected;
      return this;
    }

    public Builder key(long key) {
      this.key = key;
      return this;
    }

    public AddressRuleTest build() {
      return new AddressRuleTest(this);
    }
  }
}
