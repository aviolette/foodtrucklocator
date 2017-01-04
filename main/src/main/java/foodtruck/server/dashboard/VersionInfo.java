package foodtruck.server.dashboard;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 1/4/17
 */
@Singleton
public class VersionInfo extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Properties prop = new Properties();
    InputStream stream = getClass().getClassLoader()
        .getResourceAsStream("appinfo.properties");
    String version = "UNKNOWN";
    if (stream != null) {
      prop.load(stream);
      version = prop.getProperty("foodtrucklocator.version");
    }
    resp.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
    resp.getWriter()
        .print(version);
  }
}
