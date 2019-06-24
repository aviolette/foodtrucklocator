package foodtruck.server.vendor;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import foodtruck.model.Truck;
import foodtruck.server.dashboard.ServletImageUploader;

/**
 * @author aviolette
 * @since 2/15/17
 */
@Singleton
public class VendorImageUploadServlet extends HttpServlet {

  private final ServletImageUploader helper;

  @Inject
  public VendorImageUploadServlet(ServletImageUploader helper) {
    this.helper = helper;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Truck truck = (Truck) req.getAttribute(VendorPageFilter.TRUCK);
    helper.uploadTruckStopImage(req, resp, truck.getId());
  }
}
