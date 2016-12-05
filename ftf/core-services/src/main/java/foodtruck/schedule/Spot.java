package foodtruck.schedule;

/**
 * @author aviolette
 * @since 3/29/16
 */
public class Spot {
  private final String searchForm;
  private final String canonicalForm;

  public Spot(String searchForm, String canonicalForm) {
    this.searchForm = searchForm;
    this.canonicalForm = canonicalForm;
  }

  public boolean contains(String stripped) {
    if (process(stripped, searchForm)) {
      return true;
    }
    String split[] = searchForm.split("/");
    return split.length >= 2 && process(stripped, split[1] + "/" + split[0]);
  }

  private boolean process(String stripped, String spot) {
    return stripped.contains(spot) ||
        stripped.contains(spot.replace("/", "and")) ||
        stripped.contains(spot.replace("/", "&"));
  }

  public String getCanonicalForm() {
    return canonicalForm;
  }
}
