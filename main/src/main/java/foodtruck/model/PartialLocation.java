package foodtruck.model;

import java.util.Formatter;
import java.util.Locale;

/**
 * @author aviolette
 * @since 10/7/18
 */
public class PartialLocation extends ModelEntity {
  private final double lat, lng;
  private final String name;

  public PartialLocation(String name, double lat, double lng) {
    super(reverseLookupKey(lat, lng));
    this.name = name;
    this.lat = lat;
    this.lng = lng;
  }

  public double getLat() {
    return lat;
  }

  public double getLng() {
    return lng;
  }

  public String getName() {
    return name;
  }

  public static String reverseLookupKey(double lat, double lng) {
    StringBuilder builder = new StringBuilder();
    Formatter formatter = new Formatter(builder, Locale.US);
    formatter.format("%10.3f %10.3f", lat, lng);
    String s = builder.toString();
    return s.replaceAll(" ", "");
  }
}
