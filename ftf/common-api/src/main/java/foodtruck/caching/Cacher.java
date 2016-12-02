package foodtruck.caching;

import javax.annotation.Nullable;

/**
 * @author aviolette
 * @since 12/2/16
 */
public interface Cacher {

  public void put(String key, @Nullable Object value, int minutes);

  @Nullable
  public Object get(String key);

  public boolean contains(String key);

  public boolean delete(String key);
}
