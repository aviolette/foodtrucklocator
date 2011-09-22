package foodtruck.config;

import java.io.FileNotFoundException;
import java.util.Map;

import foodtruck.model.Truck;

/**
 * Loads the truck configurations from the configuration file.
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public interface TruckConfigParser {
  /**
   * Parses the truck configuration file
   * @param url the URL to the file
   * @return a mapping of truck Ids to trucks.
   * @throws FileNotFoundException if the file referenced by the URL cannot be found.
   */
  Map<String, Truck> parse(String url) throws FileNotFoundException;
}