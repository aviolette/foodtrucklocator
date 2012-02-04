package foodtruck.model;

/**
 * Provides an object's key to be used in a DB.
 * @author aviolette@gmail.com
 * @since 10/16/11
 */
public abstract class ModelEntity {
  protected final Object key;

  public ModelEntity(Object key) {
    this.key = key;
  }

  public Object getKey() {
    return key;
  }

  public boolean isNew() {
    return key == null || (key instanceof Long && ((Long) key) < 1);
  }
}
