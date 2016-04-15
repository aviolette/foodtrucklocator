package foodtruck.server.resources;

import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 4/14/16
 */
public interface DailySpecialResourceFactory {
  DailySpecialResource create(Truck truck);
}
