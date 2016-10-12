package foodtruck.linxup;

import java.util.List;

/**
 * @author aviolette
 * @since 7/21/16
 */
public interface LinxupConnector {
  /**
   * Finds all the positions for the authenticated user.
   */
  List<Position> findPositions();
}
