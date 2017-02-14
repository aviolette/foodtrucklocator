package foodtruck.server.dashboard;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import foodtruck.model.StaticConfig;
import foodtruck.storage.StorageService;
import foodtruck.time.Clock;

/**
 * Created by aviolette on 2/9/17.
 */
@Singleton
public class ImageUploadServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(ImageUploadServlet.class.getName());
  private final Clock clock;
  private final StorageService storageService;
  private final StaticConfig config;

  @Inject
  public ImageUploadServlet(Clock clock, StorageService storageService, StaticConfig config) {
    this.clock = clock;
    this.storageService = storageService;
    this.config = config;
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (!ServletFileUpload.isMultipartContent(request)) {
      throw new ServletException("file missing");
    }

    String bucket = "cftf_stops";
    String truck = request.getHeader("X-Dropzone-Truck");

    String filenameBase = truck + "-" + clock.now().getMillis();

    log.log(Level.INFO, "URI: {2} Bucket: {0} truck: {1}", new Object[] {bucket, filenameBase, request.getRequestURI()});
    try {
      ServletFileUpload upload = new ServletFileUpload();
      FileItemIterator iterator = upload.getItemIterator(request);
      if (iterator.hasNext()) {
        FileItemStream item = iterator.next();
        String contentType = item.getContentType();
        String extension = extensionFromContentType(contentType);
        if (extension == null) {
          // unknown image type
          log.warning("Unknown content type: " + contentType);
          return;
        }
        log.log(Level.INFO, "File {0} for field {1} {2}\n", new Object[]{ item.getName(), item.getFieldName(), contentType});
        try (InputStream stream = item.openStream()) {
          response.getWriter().print(storageService.syncStream(stream, bucket, filenameBase + "." + extension));
        }
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      throw new ServletException(e);
    }
  }

  private @Nullable String extensionFromContentType(String contentType) {
    switch (contentType) {
      case "image/png":
        return "png";
      case "image/jpeg":
        return "jpg";
      default:
        return null;
    }
  }
}
