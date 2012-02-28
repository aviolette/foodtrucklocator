// Copyright 2010 BrightTag, Inc. All rights reserved.
package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.model.Trucks;

/**
 * Endpoint used to sync
 * @author aviolette@gmail.com
 * @since 2/27/12
 */
@Singleton
public class SyncTrucksServlet extends HttpServlet {
  private final Trucks trucks;
  private final TruckDAO truckDAO;

  @Inject
  public SyncTrucksServlet(Trucks trucks, TruckDAO truckDAO) {
    this.trucks = trucks;
    this.truckDAO = truckDAO;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    for (Truck truck : trucks.allTrucks()) {
      if (truckDAO.findById(truck.getId()) == null) {
        truckDAO.save(truck);
      }
    }
  }
}
