package foodtruck.linxup;

import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;

import foodtruck.model.LinxupAccount;
import foodtruck.util.ServiceException;

/**
 * @author aviolette
 * @since 7/21/16
 */
interface LinxupConnector {
  /**
   * Finds all the positions for the authenticated user.
   * @throws IOException if there's some kind of connectivity issue
   * @throws ServiceException if there's an error in the returned payload
   */
  List<Position> findPositions(LinxupAccount account) throws IOException, ServiceException;

  /**
   * Finds history for a device over a time perieod
   */
  LinxupMapHistoryResponse tripList(LinxupAccount account, DateTime start, DateTime end, String deviceId);
}
