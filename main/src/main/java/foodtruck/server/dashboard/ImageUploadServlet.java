package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Created by aviolette on 2/9/17.
 */
@Singleton
public class ImageUploadServlet extends HttpServlet {

  private final ImageUploadHelper helper;

  @Inject
  public ImageUploadServlet(ImageUploadHelper helper) {
    this.helper = helper;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String truck = request.getHeader("X-Dropzone-Truck");

    if (!ServletFileUpload.isMultipartContent(request)) {
      throw new ServletException("file missing");
    }

    helper.uploadTruckStopImage(request, response, truck);
  }
}
