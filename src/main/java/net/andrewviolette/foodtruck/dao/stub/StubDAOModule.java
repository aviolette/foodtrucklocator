package net.andrewviolette.foodtruck.dao.stub;

import com.google.inject.AbstractModule;

import net.andrewviolette.foodtruck.dao.TruckStopDAO;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class StubDAOModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TruckStopDAO.class).to(TruckStopDAOStub.class);
  }
}
