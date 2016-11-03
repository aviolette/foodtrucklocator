package foodtruck.linxup;

import java.util.List;

import org.joda.time.DateTime;

import foodtruck.model.LinxupAccount;

/**
 * @author aviolette
 * @since 7/21/16
 */
public interface LinxupConnector {
  /**
   * Finds all the positions for the authenticated user.
   */
  List<Position> findPositions(LinxupAccount account);

  LinxupMapHistoryResponse tripList(LinxupAccount account, DateTime start, DateTime end, String deviceId);
}
