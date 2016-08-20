package foodtruck.model;

/**
 * @author aviolette
 * @since 8/17/16
 */
public class Menu extends ModelEntity {
  private final String truckId, payload;

  private Menu(Builder builder) {
    super(builder.key);
    this.truckId = builder.truckId;
    this.payload = builder.payload;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Menu menu) {
    return menu == null ? builder() : new Builder(menu);
  }

  public String getTruckId() {
    return truckId;
  }

  public String getPayload() {
    return payload;
  }

  public static class Builder {
    private String truckId, payload;
    private Object key;

    public Builder() {
    }

    public Builder(Menu menu) {
      this.key = menu.key;
      this.truckId = menu.truckId;
      this.payload = menu.payload;
    }

    public Builder truckId(String truckId) {
      this.truckId = truckId;
      return this;
    }

    public Builder key(Object key) {
      this.key = key;
      return this;
    }

    public Builder payload(String payload) {
      this.payload = payload;
      return this;
    }

    public Menu build() {
      return new Menu(this);
    }
  }
}
