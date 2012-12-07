package foodtruck.dao.appengine;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.ImmutableSet;

import foodtruck.dao.DAO;
import foodtruck.model.ModelEntity;

/**
 * @author aviolette@gmail.com
 * @since 4/12/12
 */
public abstract class AppEngineDAO<K, T extends ModelEntity> implements DAO<K, T> {
  private final String kind;
  protected final DatastoreServiceProvider provider;

  public AppEngineDAO(String kind, DatastoreServiceProvider provider) {
    this.kind = kind;
    this.provider = provider;
  }

  @Override
  public Set<T> findAll() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(getKind());
    modifyFindAllQuery(q);
    ImmutableSet.Builder<T> objs = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      T obj = fromEntity(entity);
      objs.add(obj);
    }
    return objs.build();
  }

  protected void modifyFindAllQuery(Query q) {
  }

  public void delete(long id) {
    DatastoreService dataStore = provider.get();
    Key key = KeyFactory.createKey(getKind(), id);
    dataStore.delete(key);
  }

  @Override
  public long save(T obj) {
    obj.validate();
    DatastoreService dataStore = provider.get();
    return save(obj, dataStore);
  }

  protected long save(T obj, DatastoreService dataStore) {
    Entity entity;
    if (!obj.isNew()) {
      try {
        Object theKey = obj.getKey();
        Key key;
        if (theKey instanceof Long) {
          key = KeyFactory.createKey(getKind(), (Long) theKey);
        } else if (theKey instanceof String) {
          key = KeyFactory.createKey(getKind(), (String) theKey);
        } else {
          key = (Key) theKey;
        }
        entity = dataStore.get(key);
      } catch (EntityNotFoundException e) {
        /* TODO: don't like. Had to do this because there's no way of determining whether a
       string-keyed object is new or not.
        */
        entity = buildEntity(obj);
      }
    } else {
      entity = buildEntity(obj);
    }
    entity = toEntity(obj, entity);
    Key key = dataStore.put(entity);
    return key.getId();
  }


  private Entity buildEntity(T obj) {
    final Entity entity;
    Object id = obj.getKey();
    if (id != null && id instanceof String) {
      entity = new Entity(getKind(), (String) id);
    } else {
      entity = new Entity(getKind());
    }
    return entity;
  }

  protected boolean getBooleanProperty(Entity entity, String propertyName, boolean defaultValue) {
    if (!entity.hasProperty(propertyName)) {
      return defaultValue;
    }
    return (Boolean) entity.getProperty(propertyName);
  }


  protected abstract Entity toEntity(T obj, Entity entity);

  protected abstract T fromEntity(Entity entity);

  public String getKind() {
    return kind;
  }

  public @Nullable T findById(Long id) {
    DatastoreService dataStore = provider.get();
    Key key = KeyFactory.createKey(getKind(), id);
    try {
      Entity entity = dataStore.get(key);
      return fromEntity(entity);
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  public @Nullable T findById(String id) {
    DatastoreService dataStore = provider.get();
    Key key = KeyFactory.createKey(getKind(), id);
    try {
      Entity entity = dataStore.get(key);
      return fromEntity(entity);
    } catch (EntityNotFoundException e) {
      return null;
    }
  }
}
