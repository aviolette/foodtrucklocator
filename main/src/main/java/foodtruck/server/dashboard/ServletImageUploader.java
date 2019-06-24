package foodtruck.server.dashboard;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import foodtruck.storage.StorageService;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2/15/17
 */
public class ServletImageUploader {

  private static final Logger log = Logger.getLogger(ServletImageUploader.class.getName());
  private final StorageService storageService;
  private final Clock clock;

  @Inject
  public ServletImageUploader(StorageService storageService, Clock clock) {
    this.storageService = storageService;
    this.clock = clock;
  }

  public void uploadTruckStopImage(HttpServletRequest request, HttpServletResponse response,
      String truck) throws ServletException {
    String bucket = "cftf_stops";
    String filenameBase = truck + "-" + clock.now()
        .getMillis();
    uploadImage(request, response, bucket, filenameBase);
  }

  void uploadImage(HttpServletRequest request, HttpServletResponse response, String bucket,
      String filenameBase) throws ServletException {
    log.log(Level.INFO, "URI: {2} Bucket: {0} truck: {1}", new Object[]{bucket, filenameBase, request.getRequestURI()});
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
        log.log(Level.INFO, "File {0} for field {1} {2}\n",
            new Object[]{item.getName(), item.getFieldName(), contentType});
        try (InputStream stream = item.openStream()) {
          response.getWriter()
              .print(storageService.writeImage(stream, bucket, filenameBase + "." + extension));
        }
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      throw new ServletException(e);
    }
  }

  @Nullable
  private String extensionFromContentType(String contentType) {
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
