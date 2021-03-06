package foodtruck.model;

import javax.annotation.Nullable;

/**
 * Provides an object's key to be used in a DB.
 * @author aviolette@gmail.com
 * @since 10/16/11
 */
public abstract class ModelEntity {
  protected @Nullable Object key;

  // for serialization
  public ModelEntity() {
  }

  public ModelEntity(Object key) {
    this.key = key;
  }

  public Object getKey() {
    return key;
  }

  public boolean isNew() {
    return key == null || (key instanceof Long && ((Long) key) < 1);
  }

  /**
   * Validates that the model object enough to be persisted.
   * @throws IllegalStateException if the object is not valid.
   */
  public void validate() throws IllegalStateException {
  }
}
