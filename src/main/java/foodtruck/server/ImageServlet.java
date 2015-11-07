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

import com.google.api.client.util.Strings;
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
      boolean isIpad = req.getHeader("user-agent").toLowerCase().contains("ipad");
      GcsFilename fileName = getFileName(req, isIpad);
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

  private GcsFilename getFileName(HttpServletRequest req, boolean isIpad) throws FileNotFoundException {
    String[] splits = req.getRequestURI().split("/");
    if (splits.length != 4) {
      throw new FileNotFoundException("Invalid file");
    } else if (splits[3].toLowerCase().contains(".php")) {
      throw new FileNotFoundException("File does not appear to be an image");
    }
    String fileName = splits[3];
    if (fileName.endsWith("_banner") || fileName.endsWith("_bannerlarge") || fileName.endsWith("_preview")) {
      int lastIndex = fileName.lastIndexOf("_");
      fileName = fileNameFrom(fileName, fileName.substring(lastIndex+1), isIpad);
    }
    log.log(Level.INFO, "Retrieving image: {0}", fileName);
    return new GcsFilename(staticConfig.getIconBucket(), fileName);
  }

  private String fileNameFrom(final String fileName, String suffix,
      boolean isIpad) throws FileNotFoundException {
    int last = fileName.lastIndexOf("_");
    String truckId = fileName.substring(0, last);
    Truck truck = truckDAO.findById(truckId);
    if (truck == null) {
      throw new FileNotFoundException("Invalid truck: " + truckId);
    }
    String imageUrl;
    switch(suffix) {
      case "banner":
        log.log(Level.INFO, "banner image for truck: {0}", truck.getId());
        imageUrl = truck.getBackgroundImage();
        if (!isIpad) {
          break;
        }
      case "bannerlarge":
        log.log(Level.INFO, "ipad banner image for truck: {0}", truck.getId());
        imageUrl = truck.getBackgroundImageLarge();
        if (Strings.isNullOrEmpty(imageUrl)) {
          imageUrl = truck.getBackgroundImage();
          suffix = "banner";
        }
        break;
      default:
        imageUrl = truck.getPreviewIcon();
    }
    if (imageUrl == null) {
      throw new FileNotFoundException(fileName);
    }
    String type = imageUrl.endsWith("png") ? "png" : "jpg";
    return truck.getId() + "_" + suffix + "." + type;
  }
}
