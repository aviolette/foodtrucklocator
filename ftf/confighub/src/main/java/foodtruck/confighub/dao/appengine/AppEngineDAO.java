package foodtruck.confighub.dao.appengine;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.cloud.datastore.BaseEntity;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.NullValue;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.Value;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;

import foodtruck.dao.DAO;
import foodtruck.model.ModelEntity;

/**
 * @author aviolette@gmail.com
 * @since 4/12/12
 */
abstract class AppEngineDAO<K, T extends ModelEntity> implements DAO<K, T> {
  protected final Datastore datastore;
  private final String kind;
  protected KeyFactory keyFactory;

  AppEngineDAO(String kind, Datastore datastore) {
    this.kind = kind;
    this.datastore = datastore;
    this.keyFactory = datastore.newKeyFactory().kind(kind);
  }

  @Nullable
  @Override
  public T findById(K id) {
    Entity bookEntity = datastore.get(getKey(id));
    if (bookEntity == null) {
      return null;
    }
    return fromEntity(bookEntity);
  }

  @Override
  public long count() {
    return 0;
  }

  @Override
  public List<T> findAll() {
    Query q = query();
    modifyFindAllQuery(q);
    return executeQuery(q, null);
  }

  List<T> executeQuery(Query q, @Nullable final Predicate<Entity> predicate) {
    ImmutableList.Builder<T> objs = ImmutableList.builder();
    QueryResults<Entity> results = datastore.run(q);
    while (results.hasNext()) {
      Entity entity = results.next();
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
    datastore.delete(getKey(id));
  }

  void deleteFromQuery(Query q) {
    ImmutableList.Builder<Key> keys = ImmutableList.builder();
    QueryResults<Entity> results = datastore.run(q);
    while(results.hasNext()) {
      keys.add(results.next().key());
    }
    datastore.delete(keys.build().toArray(new Key[] {}));
  }

  @Override
  public long save(T obj) {
    Object key = obj.getKey();
    if (key instanceof Long) {
      if ((long)key < 1L) {
        return create(obj);
      } else {
        return update(obj);
      }
    } else {
      Object item = datastore.get(getKey(obj.getKey()));
      if (item == null) {
        return create(obj);
      }
      return update(obj);
    }
  }

  private long update(T obj) {
    Key key = getKey(obj.getKey());
    Entity.Builder builder = Entity.builder(key);
    toEntity(obj, builder);
    datastore.update(builder.build());
    return 0;
  }

  private long create(T obj) {
    IncompleteKey key = makeKey(obj);
    FullEntity.Builder<IncompleteKey> builder = Entity.builder(key);
    toEntity(obj, builder);
    Entity bookEntity = datastore.add(builder.build());
    return 0;
  }

  protected IncompleteKey makeKey(T obj) {
    return keyFactory.newKey();
  }

  protected Key getKey(Object theKey) {
    Key key;
    if (theKey instanceof Long) {
      key = keyFactory.newKey((Long) theKey);
    } else if (theKey instanceof String) {
      key = keyFactory.newKey((String) theKey);
    } else {
      key = (Key) theKey;
    }
    return key;
  }


  /**
   * Builds a new version of the object form the entity specified
   * @param entity the entity
   * @return a new object
   */

  protected abstract T fromEntity(Entity entity);

  /**
   * Serializes the objects properties to the specified entity
   * @param obj the object
   * @return the entity passed in
   */
  protected abstract void toEntity(T obj, BaseEntity.Builder builder);

  protected Query query() {
    return Query.entityQueryBuilder()
        .kind(getKind())
        .build();
  }


  public String getKind() {
    return kind;
  }

  static @Nullable String getString(Entity entity, String propertyName) {
    try {
      return entity.getString(propertyName);
    } catch (DatastoreException dse) {
      return null;
    }
  }

  static @Nullable boolean getBoolean(Entity entity, String propertyName) {
    try {
      return entity.getBoolean(propertyName);
    } catch (DatastoreException dse) {
      return false;
    }
  }

  static @Nullable long getLong(Entity entity, String propertyName) {
    try {
      return entity.getLong(propertyName);
    } catch (DatastoreException dse) {
      return 0L;
    }
  }


  static List<String> getList(String propertyName, Entity entity) {
    try {
      List<Value<String>> list = entity.getList(propertyName);
      if (list == null) {
        return ImmutableList.of();
      }
      return ImmutableList.copyOf(list.stream()
          .map(Value::get)
          .collect(Collectors.toList()));
    } catch (DatastoreException dse) {
      return ImmutableList.of();
    }
  }

  static ImmutableSet<String> getSet(String propertyName, Entity entity) {
    try {
      List<Value<String>> list = entity.getList(propertyName);
      if (list == null) {
        return ImmutableSet.of();
      }
      return ImmutableSet.copyOf(list.stream()
          .map(Value::get)
          .collect(Collectors.toSet()));
    } catch (DatastoreException dse) {
      return ImmutableSet.of();
    }
  }

  static void setDateProperty(String propertyName, BaseEntity.Builder entity, @Nullable DateTime dateTime) {
    if (dateTime == null) {
      entity.set(propertyName, NullValue.of());
    } else {
      com.google.cloud.datastore.DateTime dt = com.google.cloud.datastore.DateTime.copyFrom(dateTime.toDate());
      entity.set(propertyName, dt);
    }
  }

}
