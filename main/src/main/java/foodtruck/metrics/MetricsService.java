package foodtruck.metrics;

import java.util.Map;

import foodtruck.monitoring.StatUpdate;

/**
 * @author aviolette
 * @since 2019-01-09
 */
public interface MetricsService {

  void updateStats(Map<StatUpdate, Integer> counts);
}
