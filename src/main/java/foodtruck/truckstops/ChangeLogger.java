// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.truckstops;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;

import foodtruck.dao.TruckStopChangeDAO;
import foodtruck.model.TruckStop;
import foodtruck.model.TruckStopChange;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since 5/24/12
 */
public class ChangeLogger implements TruckStopNotifier {
  private final TruckStopChangeDAO truckStopChangeDAO;
  private final Clock clock;
  private static final Logger log = Logger.getLogger(ChangeLogger.class.getName());

  @Inject
  public ChangeLogger(TruckStopChangeDAO dao, Clock clock) {
    this.truckStopChangeDAO = dao;
    this.clock = clock;
  }

  @Override public void added(TruckStop stop) {
    log.log(Level.INFO, "Stop added: {0}", stop);
    TruckStopChange.Builder changeBuilder = TruckStopChange.builder();
    changeBuilder.from(null).to(stop).timeStamp(clock.now());
    truckStopChangeDAO.save(changeBuilder.build());
  }

  @Override public void removed(TruckStop stop) {
    log.log(Level.INFO, "Stop removed: {0}", stop);
    TruckStopChange.Builder changeBuilder = TruckStopChange.builder();
    changeBuilder.to(null).from(stop).timeStamp(clock.now());
    truckStopChangeDAO.save(changeBuilder.build());
  }

  @Override public void terminated(TruckStop stop) {
    TruckStopChange.Builder changeBuilder = TruckStopChange.builder();
    changeBuilder.from(null).to(stop).timeStamp(clock.now());
    truckStopChangeDAO.save(changeBuilder.build());
  }
}
