package foodtruck.appengine.dao.memcached;

import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.collect.ForwardingObject;

/**
 * Base class for memcached-fronted DAO's
 * @author aviolette
 * @since 3/16/15
 */
public abstract class AbstractMemcachedDAO<T> extends ForwardingObject {
  private static final Logger log = Logger.getLogger(AbstractMemcachedDAO.class.getName());
  final MemcacheService memcacheService;
  private final T delegate;
  private final String prefix;

  AbstractMemcachedDAO(T dao, String prefix, MemcacheService memcacheService) {
    this.delegate = dao;
    this.prefix = prefix;
    this.memcacheService = memcacheService;
  }

  protected T delegate() {
    return delegate;
  }

  void invalidateAll() {
    log.log(Level.INFO, "Invalidating memcached for " + prefix);
    memcacheService.clearAll();
  }

  String keyName(String suffix) {
    return prefix + ":" + suffix;
  }

  @SuppressWarnings("unchecked")
  <M> M delegateIf(String name, Function<T, M> func) {
    String fullName = keyName(name);
    M rc = (M)memcacheService.get(fullName);
    if (rc != null) {
      log.log(Level.FINE, "Retrieved {0} from cache", fullName);
      return rc;
    }
    log.log(Level.FINE, "Retrieving {0} from DB", fullName);
    rc = func.apply(delegate());
    try {
      memcacheService.put(fullName, rc);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Couldn't store in memcached " + fullName, e);
    }
    return rc;
  }
}
