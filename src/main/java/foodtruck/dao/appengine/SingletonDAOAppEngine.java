package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

import foodtruck.dao.SingletonDAO;
import foodtruck.model.ModelEntity;

/**
 * @author aviolette@gmail.com
 * @since 9/13/12
 */
public abstract class SingletonDAOAppEngine<E extends ModelEntity> implements SingletonDAO<E> {
  private final DatastoreServiceProvider provider;
  private String kind;

  public SingletonDAOAppEngine(DatastoreServiceProvider provider, String kind) {
    this.provider = provider;
    this.kind = kind;
  }

  @Override public E find() {
    DatastoreService service = provider.get();
    Query q = new Query(getKind());
    Entity entity = service.prepare(q).asSingleEntity();
    if (entity == null) {
      return buildObject();
    }
    return fromEntity(entity);
  }

  @Override public void save(E obj) {
    DatastoreService service = provider.get();
    Query q = new Query(getKind());
    Entity entity = service.prepare(q).asSingleEntity();
    if (entity == null) {
      entity = new Entity(getKind());
    }
    entity = toEntity(entity, obj);
    service.put(toEntity(entity, obj));
  }

  /**
   * Builds a new version of the object form the entity specified
   * @param entity the entity
   * @return a new object
   */

  protected abstract E fromEntity(Entity entity);

  /**
   * Serializes the objects properties to the specified entity
   * @param entity the entity
   * @param obj the object
   * @return the entity passed in
   */
  protected abstract Entity toEntity(Entity entity, E obj);

  /**
   * Constructs an initial version of the object
   */
  protected abstract E buildObject();

  /**
   * Returns the datastore collection name
   */
  protected String getKind() {
    return kind;
  }
}
