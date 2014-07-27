package foodtruck.model;

import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkState;

/**
 * Provides an object's key to be used in a DB.
 * @author aviolette@gmail.com
 * @since 10/16/11
 */
public abstract class ModelEntity {
  public static final long UNINITIALIZED = -1;
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

  /**
   * Validates that the model object enough to be persisted.
   * @throws IllegalStateException if the object is not valid.
   */
  public void validate() throws IllegalStateException {
  }
}
