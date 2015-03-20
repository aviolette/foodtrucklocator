package foodtruck.dao.memcached;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.common.base.Function;
import com.google.common.collect.ForwardingObject;

/**
 * Base class for memcached-fronted DAO's
 * @author aviolette
 * @since 3/16/15
 */
public abstract class AbstractMemcachedDAO<T> extends ForwardingObject {
  private static final Logger log = Logger.getLogger(AbstractMemcachedDAO.class.getName());

  private final T delegate;
  private final String prefix;
  protected final MemcacheService memcacheService;

  protected AbstractMemcachedDAO(T dao, String prefix, MemcacheService memcacheService) {
    this.delegate = dao;
    this.prefix = prefix;
    this.memcacheService = memcacheService;
  }

  protected T delegate() {
    return delegate;
  }

  protected void invalidateAll() {
    log.log(Level.INFO, "Invalidating memcached for " + prefix);
    memcacheService.clearAll();
  }

  protected String keyName(String suffix) {
    return prefix + ":" + suffix;
  }

  protected <M> M delegateIf(String name, Function<T, M> func) {
    String fullName = keyName(name);
    M rc = (M)memcacheService.get(fullName);
    if (rc != null) {
      log.log(Level.INFO, "Retrieved {0} from cache", fullName);
      return rc;
    }
    log.log(Level.INFO, "Retrieving {0} from DB", fullName);
    rc = func.apply(delegate());
    try {
      memcacheService.put(fullName, rc);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Couldn't store in memcached " + fullName, e);
    }
    return rc;
  }
}
