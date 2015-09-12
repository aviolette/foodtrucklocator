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

import foodtruck.model.StaticConfig;
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

  @Inject
  public ImageServlet(GcsService cloudStorage, @HttpHeaderFormat DateTimeFormatter formatter, StaticConfig staticConfig) {
    this.cloudStorage = cloudStorage;
    this.dateFormatter = formatter;
    this.staticConfig = staticConfig;
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
    return new GcsFilename(staticConfig.getIconBucket(), splits[3]);
  }
}
