package foodtruck.truckstops;

import java.util.logging.Level;
import java.util.logging.Logger;

import foodtruck.model.TruckStop;

/**
 * @author aviolette@gmail.com
 * @since 5/24/12
 */
public class LoggingTruckStopNotifier implements TruckStopNotifier {
  private static final Logger log = Logger.getLogger(LoggingTruckStopNotifier.class.getName());

  @Override public void added(TruckStop stop) {
    log.log(Level.INFO, "Stop added: {0}", stop);
  }

  @Override public void removed(TruckStop stop) {
    log.log(Level.INFO, "Stop removed: {0}", stop);
  }

  @Override public void terminated(TruckStop stop) {
    log.log(Level.INFO, "Stop terminated: {0}", stop);
  }
}
