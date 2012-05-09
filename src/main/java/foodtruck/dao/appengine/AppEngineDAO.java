// Copyright 2012 BrightTag, Inc. All rights reserved.
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

import foodtruck.model.ModelEntity;

/**
 * @author aviolette@gmail.com
 * @since 4/12/12
 */
public abstract class AppEngineDAO<T extends ModelEntity> {
  private final String kind;
  protected final DatastoreServiceProvider provider;

  public AppEngineDAO(String kind, DatastoreServiceProvider provider) {
    this.kind = kind;
    this.provider = provider;
  }

  public Set<T> findAll() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(getKind());
    ImmutableSet.Builder<T> objs = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      T obj = fromEntity(entity);
      objs.add(obj);
    }
    return objs.build();
  }

  public @Nullable T findById(String id) {
    DatastoreService dataStore = provider.get();
    try {
      return fromEntity(dataStore.get(KeyFactory.createKey(kind, id)));
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  public void save(T obj) {
    DatastoreService dataStore = provider.get();
    Entity entity = null;
    try {
      if (!obj.isNew()) {
        Object theKey = obj.getKey();
        Key key;
        if (theKey instanceof Long) {
          key = KeyFactory.createKey(getKind(), (Long) theKey);
        } else {
          key = (Key) theKey;
        }
        entity = dataStore.get(key);
      }
      entity = toEntity(obj, entity);
      dataStore.put(entity);
    } catch (EntityNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract Entity toEntity(T obj, Entity entity);

  protected abstract T fromEntity(Entity entity);

  public String getKind() {
    return kind;
  }
}
