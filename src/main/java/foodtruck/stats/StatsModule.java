package foodtruck.stats;

import com.google.inject.AbstractModule;

/**
 * @author aviolette
 * @since 5/18/14
 */
public class StatsModule extends AbstractModule {
  @Override protected void configure() {
    bind(HeatmapService.class).to(HeatmapServiceImpl.class);
  }
}
