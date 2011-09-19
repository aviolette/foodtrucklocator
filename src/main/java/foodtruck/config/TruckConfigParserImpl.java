package foodtruck.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.yaml.snakeyaml.Yaml;

import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class TruckConfigParserImpl implements TruckConfigParser {
  private static final Logger log = Logger.getLogger(TruckConfigParserImpl.class.getName());

  @Override
  public Map<String, Truck> parse(String url)
      throws FileNotFoundException {
    Yaml yaml = new Yaml();
    Iterable<Map<String, Object>> trucks =
        (Iterable) yaml.loadAll(new BufferedReader(new FileReader(url)));
    ImmutableMap.Builder<String, Truck> truckBuilder = ImmutableMap.builder();
    for (Map<String, Object> truckMap : trucks) {
      Truck truck = new Truck.Builder()
          .id((String) truckMap.get("id"))
          .name((String) truckMap.get("name"))
          .url((String) truckMap.get("url"))
          .iconUrl((String) truckMap.get("iconUrl"))
          .description((String) truckMap.get("description"))
          .categories(splitList(truckMap.get("categories")))
          .twitterHandle((String) truckMap.get("twitter"))
          .build();
      log.log(Level.INFO, "Loaded truck: {0}", truck);
      truckBuilder.put(truck.getId(), truck);
    }
    return truckBuilder.build();
  }

  private ImmutableSet<String> splitList(Object categories) {
    String categoryList = (String) categories;
    return categoryList == null ? ImmutableSet.<String>of() :
        ImmutableSet.copyOf(categoryList.split(","));
  }
}
