package foodtruck.truckstops;

import com.google.inject.AbstractModule;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class ServiceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(FoodTruckStopService.class).to(FoodTruckStopServiceImpl.class);
  }
}
