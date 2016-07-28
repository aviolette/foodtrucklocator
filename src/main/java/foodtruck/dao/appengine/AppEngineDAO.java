package foodtruck.dao.appengine;

import java.util.List;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import foodtruck.dao.DAO;
import foodtruck.model.ModelEntity;
import foodtruck.model.TruckStop;

/**
 * @author aviolette@gmail.com
 * @since 4/12/12
 */
public abstract class AppEngineDAO<K, T extends ModelEntity> implements DAO<K, T> {
  protected final DatastoreServiceProvider provider;
  private final String kind;

  public AppEngineDAO(String kind, DatastoreServiceProvider provider) {
    this.kind = kind;
    this.provider = provider;
  }

  @Override
  public List<T> findAll() {
    Query q = query();
    modifyFindAllQuery(q);
    return executeQuery(q, null);
  }


  List<T> executeQuery(Query q) {
    return executeQuery(q, null);
  }

  List<T> executeQuery(Query q, @Nullable Predicate<Entity> predicate) {
    DatastoreService dataStore = provider.get();
    ImmutableList.Builder<T> objs = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      if (predicate != null && !predicate.apply(entity)) {
        continue;
      }
      T obj = fromEntity(entity);
      objs.add(obj);
    }
    return objs.build();
  }

  protected void modifyFindAllQuery(Query q) {
  }

  @Override
  public void delete(K id) {
    DatastoreService dataStore = provider.get();
    dataStore.delete(getKey(id));
  }

  void deleteFromQuery(DatastoreService dataStore, Query q) {
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      keys.add(entity.getKey());
    }
    dataStore.delete(keys.build());
  }

  @Override
  public long save(T obj) {
    obj.validate();
    DatastoreService dataStore = provider.get();
    return save(obj, dataStore);
  }

  @Override
  public long count() {
    DatastoreService dataStore = provider.get();
    FetchOptions options = FetchOptions.Builder.withDefaults();
    // TODO: this won't work properly, but works for my existing use case.
    return dataStore.prepare(new Query(getKind())).countEntities(options);
  }

  protected long save(T obj, DatastoreService dataStore) {
    Entity entity;
    if (!obj.isNew()) {
      try {
        entity = dataStore.get(getKey(obj.getKey()));
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

  protected Key getKey(Object theKey) {
    Key key;
    if (theKey instanceof Long) {
      key = KeyFactory.createKey(getKind(), (Long) theKey);
    } else if (theKey instanceof String) {
      key = KeyFactory.createKey(getKind(), (String) theKey);
    } else {
      key = (Key) theKey;
    }
    return key;
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

  boolean getBooleanProperty(Entity entity, String propertyName, boolean defaultValue) {
    if (!entity.hasProperty(propertyName)) {
      return defaultValue;
    }
    return (Boolean) entity.getProperty(propertyName);
  }


  protected abstract Entity toEntity(T obj, Entity entity);

  protected abstract T fromEntity(Entity entity);

  protected Query query() {
    return new Query(getKind());
  }


  protected Query.FilterPredicate predicate(String name, Query.FilterOperator operator, Object value) {
    return new Query.FilterPredicate(name, operator, value);
  }


  protected AQ aq() {
    return new AQ();
  }

  public String getKind() {
    return kind;
  }

  @Nullable
  T findSingleItemByAttribute(String attributeName, String attributeValue) {
    return findSingleItemByFilter(new Query.FilterPredicate(attributeName, Query.FilterOperator.EQUAL, attributeValue));
  }

  protected @Nullable T findSingleItemByFilter(Query.Filter filter) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(getKind());
    q.setFilter(filter);
    Entity entity = dataStore.prepare(q).asSingleEntity();
    return (entity == null) ? null : fromEntity(entity);
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

  protected class AQ {
    private Query query;

    private AQ() {
      query = new Query(getKind());
    }

    public List<T> execute() {
      return executeQuery(query, null);
    }

    public @Nullable T findOne() {
      Entity entity = provider.get().prepare(query).asSingleEntity();
      return (entity == null) ? null : fromEntity(entity);
    }

    public AQ filter(Query.Filter filter) {
      query.setFilter(filter);
      return this;
    }

    public AQ sort(String nameField) {
      query.addSort(nameField);
      return this;
    }

    public AQ sort(String nameField, Query.SortDirection direction) {
      query.addSort(nameField, direction);
      return this;
    }

    public List<T> execute(Predicate<Entity> predicate) {
      return executeQuery(query, predicate);
    }
  }
}
