package foodtruck.dao.ws;

import com.google.inject.AbstractModule;

import foodtruck.dao.TruckDAO;

/**
 * @author aviolette
 * @since 12/5/16
 */
public class WebClientDAOModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TruckDAO.class).to(TruckDAOWebClient.class);
  }
}
