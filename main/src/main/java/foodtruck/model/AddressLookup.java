package foodtruck.model;

public class AddressLookup {
  private final String text;
  private final Truck truck;

  public AddressLookup(String text, Truck truck) {
    this.text = text;
    this.truck = truck;
  }

  public String getText() {
    return text;
  }

  public Truck getTruck() {
    return truck;
  }

  @Override
  public String toString() {
    return "AddressLookup{" + "text='" + text + '\'' + ", truck=" + truck + '}';
  }
}
