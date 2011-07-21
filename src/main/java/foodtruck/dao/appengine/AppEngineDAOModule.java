package foodtruck.dao.appengine;

import com.google.inject.AbstractModule;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckStopDAO;

/**
 * @author aviolette@gmail.com
 * @since Jul 13, 2011
 */
public class AppEngineDAOModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TruckStopDAO.class).to(TruckStopDAOAppEngine.class);
    bind(LocationDAO.class).to(LocationDAOAppEngine.class);
  }
}
