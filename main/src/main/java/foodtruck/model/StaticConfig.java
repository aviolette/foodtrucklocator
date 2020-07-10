package foodtruck.model;

/**
 * @author aviolette
 * @since 1/5/15
 */
public class StaticConfig {


  public String getPrimaryTwitterList() {
    return System.getProperty("foodtrucklocator.twitter.list.id");
  }

  public String getPrimaryTwitterListSlug() {
    return System.getProperty("foodtrucklocator.twitter.list.slug");
  }

  public String getPrimaryTwitterListOwner() {
    return System.getProperty("foodtrucklocator.twitter.list.owner");
  }


}
