package foodtruck.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.util.HttpHeaderFormat;

/**
 * @author aviolette
 * @since 12/29/14
 */
@Singleton
public class ImageServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(ImageServlet.class.getName());
  private final GcsService cloudStorage;
  private final DateTimeFormatter dateFormatter;

  @Inject
  public ImageServlet(GcsService cloudStorage, @HttpHeaderFormat DateTimeFormatter formatter) {
    this.cloudStorage = cloudStorage;
    this.dateFormatter = formatter;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    GcsFilename fileName = getFileName(req);
    try {
      GcsInputChannel readChannel = cloudStorage.openPrefetchingReadChannel(fileName, 0, 2097152);
      InputStream in = Channels.newInputStream(readChannel);
      try {
        ByteStreams.copy(Channels.newInputStream(readChannel), resp.getOutputStream());
      } finally {
        in.close();
      }
      GcsFileMetadata metaData = cloudStorage.getMetadata(fileName);
      resp.setHeader("ETag", metaData.getEtag());
      resp.setHeader("Cache-Control", "no-transform,public,max-age=300,s-max-age=900");
      resp.setHeader("Last-Modified", dateFormatter.print(new DateTime(metaData.getLastModified().getTime())));
      resp.setContentType(metaData.getOptions().getMimeType());
    } catch (FileNotFoundException fnfe) {
      log.log(Level.FINE, fnfe.getMessage());
      resp.sendError(404, "Not found: " + req.getRequestURI());
    } catch (Exception e) {
      log.log(Level.WARNING, e.getMessage(), e);
      resp.sendError(500, e.getMessage());
    }
  }

  private GcsFilename getFileName(HttpServletRequest req) {
    String[] splits = req.getRequestURI().split("/");
    return new GcsFilename("truckicons", splits[3]);
  }
}
