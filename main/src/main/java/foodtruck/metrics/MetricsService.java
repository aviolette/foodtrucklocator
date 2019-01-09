package foodtruck.metrics;

import java.util.Map;

/**
 * @author aviolette
 * @since 2019-01-09
 */
public interface MetricsService {

  void updateStats(long timestamp, Map<String, Integer> counts);
}
