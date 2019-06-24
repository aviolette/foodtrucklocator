package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import foodtruck.time.Clock;

/**
 * Created by aviolette on 2/9/17.
 */
@Singleton
public class ImageUploadServlet extends HttpServlet {

  private final ServletImageUploader helper;
  private final Clock clock;

  @Inject
  public ImageUploadServlet(ServletImageUploader helper, Clock clock) {
    this.helper = helper;
    this.clock = clock;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String type = request.getHeader("X-Dropzone-Type");
    String key = request.getHeader("X-Dropzone-Key");
    String filename = key + "-" + clock.nowInMillis();
    String bucket = typeToBucket(type);

    if (!ServletFileUpload.isMultipartContent(request)) {
      throw new ServletException("file missing");
    }

    helper.uploadImage(request, response, bucket, filename);
  }

  private String typeToBucket(String type) {
    switch(type) {
      case "stop":
        return "cftf_stops";
      case "location":
        return "cftf_locationicons";
      case "truck":
        return "truckicons";
      default:
        throw new RuntimeException("Invalid upload type: " + type);
    }
  }
}
