package foodtruck.server.dashboard;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

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

  @Inject
  public ImageUploadServlet(Clock clock, StorageService storageService) {
    this.clock = clock;
    this.storageService = storageService;
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
/*
    try {
      ServletFileUpload upload = new ServletFileUpload();
      FileItemIterator iterator = upload.getItemIterator(request);
      while (iterator.hasNext()) {
        FileItemStream item = iterator.next();
        log.log(Level.INFO, "File {0} for field {1}:\n", new Object[]{item.getName(), item.getFieldName()});
      }
    } catch (FileUploadException e) {
      throw new ServletException(e);
    }
    */
  }
}
