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

import foodtruck.dao.TruckDAO;
import foodtruck.model.StaticConfig;
import foodtruck.model.Truck;
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
  private final StaticConfig staticConfig;
  private final TruckDAO truckDAO;

  @Inject
  public ImageServlet(GcsService cloudStorage, @HttpHeaderFormat DateTimeFormatter formatter,
      StaticConfig staticConfig, TruckDAO truckDAO) {
    this.cloudStorage = cloudStorage;
    this.dateFormatter = formatter;
    this.staticConfig = staticConfig;
    this.truckDAO = truckDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      GcsFilename fileName = getFileName(req);
      GcsInputChannel readChannel = cloudStorage.openPrefetchingReadChannel(fileName, 0, 2097152);
      try (InputStream in = Channels.newInputStream(readChannel)) {
        ByteStreams.copy(in, resp.getOutputStream());
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

  private GcsFilename getFileName(HttpServletRequest req) throws FileNotFoundException {
    String[] splits = req.getRequestURI().split("/");
    if (splits.length != 4) {
      throw new FileNotFoundException("Invalid file");
    } else if (splits[3].toLowerCase().contains(".php")) {
      throw new FileNotFoundException("File does not appear to be an image");
    }
    String fileName = splits[3];
    if (fileName.endsWith("_banner")) {
      fileName = fileNameFrom(fileName, "banner");
    } else if (fileName.endsWith("_preview")) {
      fileName = fileNameFrom(fileName, "preview");
    }
    log.log(Level.INFO, "Retrieving image: {0}", fileName);
    return new GcsFilename(staticConfig.getIconBucket(), fileName);
  }

  private String fileNameFrom(final String fileName, String suffix) throws FileNotFoundException {
    int last = fileName.lastIndexOf("_");
    String truckId = fileName.substring(0, last);
    Truck truck = truckDAO.findById(truckId);
    if (truck == null) {
      throw new FileNotFoundException("Invalid truck: " + truckId);
    }
    String imageUrl = "banner".equals(suffix) ? truck.getBackgroundImage() : truck.getPreviewIcon();
    if (imageUrl == null) {
      throw new FileNotFoundException(fileName);
    }
    String type = imageUrl.endsWith("png") ? "png" : "jpg";
    return truck.getId() + "_" + suffix + "." + type;
  }
}
