package foodtruck.config;

import java.io.FileNotFoundException;
import java.util.Map;

import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public interface TruckConfigParser {
  Map<String, Truck> parse(String url) throws FileNotFoundException;
}
