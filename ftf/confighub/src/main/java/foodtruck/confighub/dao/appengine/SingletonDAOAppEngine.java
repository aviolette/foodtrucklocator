package foodtruck.confighub.dao.appengine;

import com.google.cloud.datastore.BaseEntity;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;

import foodtruck.dao.SingletonDAO;
import foodtruck.model.ModelEntity;

/**
 * @author aviolette@gmail.com
 * @since 9/13/12
 */
abstract class SingletonDAOAppEngine<E extends ModelEntity> implements SingletonDAO<E> {
  private final Datastore datastore;
  private String kind;
  private KeyFactory keyFactory;

  SingletonDAOAppEngine(Datastore datastore, String kind) {
    this.datastore = datastore;
    this.kind = kind;
    this.keyFactory = datastore.newKeyFactory().kind(kind);
  }

  @Override public E find() {
    Query<Entity> query = Query.entityQueryBuilder()
        .kind(kind)
        .limit(10)
        .startCursor(null)
        .build();
    QueryResults<Entity> rs = datastore.run(query);
    if (rs.hasNext()) {
      return fromEntity(rs.next());
    } else {
      return buildObject();
    }
  }

  @Override public void save(E obj) {
    E oldObj = find();
    System.out.println("OLD: " + oldObj);
    if (oldObj != null && !oldObj.isNew()) {
      System.out.println("HEW");
      Entity.Builder builder = Entity.builder(keyFactory.newKey((long)oldObj.getKey()));
      toEntity(obj, builder);
      datastore.update(builder.build());
    } else {
      System.out.println("NEW");
      FullEntity.Builder builder = Entity.builder(keyFactory.newKey());
      toEntity(obj, builder);
      datastore.add(builder.build());
    }
  }

  /**
   * Builds a new version of the object form the entity specified
   * @param entity the entity
   * @return a new object
   */

  protected abstract E fromEntity(Entity entity);

  /**
   * Serializes the objects properties to the specified entity
   * @param obj the object
   * @return the entity passed in
   */
  protected abstract void toEntity(E obj, BaseEntity.Builder builder);

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
