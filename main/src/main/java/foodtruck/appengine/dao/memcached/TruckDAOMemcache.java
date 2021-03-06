package foodtruck.appengine.dao.memcached;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.util.Secondary;

/**
 * @author aviolette
 * @since 3/16/15
 */
public class TruckDAOMemcache extends AbstractMemcachedDAO<TruckDAO> implements TruckDAO {
  private static final Logger log = Logger.getLogger(TruckDAOMemcache.class.getName());

  private static final String BY_TWITTER_ID = "findByTwitterId:";
  private static final String BY_ID = "findById:";
  private static final String ALL_TWITTER_TRUCKS = "twitterTrucks";
  private static final String ACTIVE_TRUCKS = "activeTrucks";
  private static final String INACTIVE_TRUCKS = "inactiveTrucks";
  private static final String FIND_FIRST = "findFirst";

  @Inject
  public TruckDAOMemcache(@Secondary TruckDAO dao, MemcacheService memcacheService) {
    super(dao, "TruckDAO", memcacheService);
  }

  @Override
  public Collection<Truck> findByTwitterId(final String screenName) {
    return delegateIf(BY_TWITTER_ID + screenName,
        truckDAO -> truckDAO.findByTwitterId(screenName));
  }

  @Override
  public List<Truck> findActiveTrucks() {
    return delegateIf(ACTIVE_TRUCKS, TruckDAO::findActiveTrucks);
  }

  @Override
  public Collection<Truck> findInactiveTrucks() {
    return delegateIf(INACTIVE_TRUCKS, TruckDAO::findInactiveTrucks);
  }

  @Override
  public Set<Truck> findTrucksWithCalendars() {
    return delegate().findTrucksWithCalendars();
  }

  @Override
  public Set<Truck> findTruckWithICalCalendars() {
    return delegate().findTruckWithICalCalendars();
  }

  @Override
  public List<Truck> findVisibleTrucks() {
    return delegate().findVisibleTrucks();
  }

  @Override
  public Truck findFirst() {
    return delegateIf(FIND_FIRST, TruckDAO::findFirst);
  }

  @Override
  public List<Truck> findByCategory(String tag) {
    return delegate().findByCategory(tag);
  }

  @Override
  public Set<Truck> findByBeaconnaiseEmail(String email) {
    return delegate().findByBeaconnaiseEmail(email);
  }

  @Override
  public void deleteAll() {
    invalidateAll();
    delegate().deleteAll();
  }

  @Nullable
  @Override
  public Truck findByName(String name) {
    return delegate().findByName(name);
  }

  @Nullable
  @Override
  public Truck findByNameOrAlias(String name) {
    return delegate().findByNameOrAlias(name);
  }


  @Override
  public List<Truck> findAll() {
    return delegate().findAll();
  }

  @Override
  public long save(Truck obj) {
    invalidateTruck(obj);
    return delegate().save(obj);
  }

  private void invalidateTruck(Truck truck) {
    log.info("Invalidating truck in cache: " + truck.getId());
    memcacheService.deleteAll(
        ImmutableList.of(keyName(BY_TWITTER_ID + truck.getTwitterHandle()), keyName(ALL_TWITTER_TRUCKS),
            keyName(ACTIVE_TRUCKS), keyName(INACTIVE_TRUCKS), keyName(FIND_FIRST), keyName(BY_ID + truck.getId())));
  }

  @Override @Deprecated
  public @Nullable Truck findById(final String id) {
    return delegateIf(BY_ID + id, truckDAO -> truckDAO.findById(id));
  }

  @Override
  public Optional<Truck> findByIdOpt(String id) {
    return Optional.ofNullable(findById(id));
  }

  @Override
  public void delete(String id) {
    invalidateAll();
    delegate().delete(id);
  }

  @Override
  public long count() {
    return delegate().count();
  }
}
