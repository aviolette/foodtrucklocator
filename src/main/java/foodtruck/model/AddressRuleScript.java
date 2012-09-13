package foodtruck.model;

import javax.annotation.Nullable;

import twitter4j.internal.org.json.JSONWriter;

/**
 * A script that is used to parse addresses.
 * @author aviolette@gmail.com
 * @since 8/19/12
 */
public class AddressRuleScript extends ModelEntity {
  private String script;

  public AddressRuleScript(Builder builder) {
    super(builder.key);
    this.script = builder.script;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getScript() {
    return script;
  }

  public static Builder builder(AddressRuleScript obj) {
    return new Builder(obj);
  }

  public static class Builder {
    private String script;
    private Long key;

    public Builder() {}

    public Builder(AddressRuleScript script) {
      this.script = script.getScript();
      this.key = (Long)script.getKey();
    }

    public Builder script(String script) {
      this.script = script;
      return this;
    }

    public Builder key(@Nullable Long key) {
      this.key = key;
      return this;
    }

    public AddressRuleScript build() {
      return new AddressRuleScript(this);
    }
  }
}
