package foodtruck.dao.memcached;

import com.google.inject.AbstractModule;

import foodtruck.dao.TruckDAO;

/**
 * @author aviolette
 * @since 3/16/15
 */
public class MemcachedModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TruckDAO.class).to(TruckDAOMemcache.class);
  }
}
