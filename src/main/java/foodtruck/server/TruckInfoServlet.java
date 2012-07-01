// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since 6/29/12
 */
@Singleton
public class TruckInfoServlet extends HttpServlet {
  private final TruckDAO truckDAO;

  @Inject
  public TruckInfoServlet(TruckDAO truckDAO) {
    this.truckDAO = truckDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String jsp = "/WEB-INF/jsp/trucks.jsp";
    final String path = req.getRequestURI();
    final String keyIndex = path.substring(path.lastIndexOf("/") + 1);
    boolean truckList = true;
    if (!Strings.isNullOrEmpty(keyIndex) && !keyIndex.startsWith("trucks")) {
      jsp = "/WEB-INF/jsp/truckView.jsp";
      truckList = false;
    }
    req = new GuiceHackRequestWrapper(req, jsp);
    if (truckList) {
      req.setAttribute("trucks", truckDAO.findActiveTrucks());
    } else {
      Truck truck = truckDAO.findById(keyIndex);
      if (truck == null) {
        resp.sendError(404);
        return;
      }
      req.setAttribute("truck", truck);
    }
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}
