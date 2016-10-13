package foodtruck.model;

/**
 * @author aviolette
 * @since 6/13/15
 */
public class ApplicationWithUsageCounts {
  private final Application application;
  private final long dailyCount;

  public ApplicationWithUsageCounts(Application application, long dailyCount) {
    this.application = application;
    this.dailyCount = dailyCount;
  }

  public Application getApplication() {
    return application;
  }

  public long getDailyCount() {
    return dailyCount;
  }
}
