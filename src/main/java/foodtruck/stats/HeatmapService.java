package foodtruck.stats;

/**
 * @author aviolette
 * @since 5/18/14
 */
public interface HeatmapService {
  /**
   * Gets the current stringified JSON (rebuilding if necessary)
   * @return
   */
  String get();

  /**
   * Rebuilds the stringified JSON for the heatmap and stores it in memecached
   */
  void rebuild();
}
