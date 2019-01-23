package foodtruck.tld;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.google.common.escape.Escaper;
import com.google.common.net.PercentEscaper;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 12/18/12
 */
public class LocationFormatTag extends TagSupport {
  private static final Logger log = Logger.getLogger(LocationFormatTag.class.getName());
  private final DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYYMMdd");
  private Location location;
  private DateTime when;
  private boolean admin;
  private boolean longFormat;
  private static final Escaper escaper =
      new PercentEscaper("-._~!$'()*,;=@:", false);

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public DateTime getAt() {
    return when;
  }

  public void setAt(DateTime dateTime) {
    when = dateTime;
  }

  public Location getLocation() {
    return this.location;
  }

  public void setLocation(@Nullable Location location) {
    this.location = location;
  }

  @SuppressWarnings("unused")
  public boolean isLongFormat() {
    return longFormat;
  }

  @SuppressWarnings("unused")
  public void setLongFormat(boolean longFormat) {
    this.longFormat = longFormat;
  }

  @Override public int doStartTag() throws JspException {
    JspWriter out = pageContext.getOut();
    String dateString = "";
    if (when != null) {
      dateString = "date=" + formatter.print(when);
    }
    try {
      if (location != null) {
        String locationName = escaper.escape(location.getName());
        if (location.getKey() != null) {
          String aString = admin ? "/admin" : "";
          out.println("<a href='" + aString + "/locations/" + location.getKey() + "?" + dateString +"'>" + name(location) + "</a>");
        } else {
          String query = admin ? "&admin=true" : "";
          out.println("<a href='/locations?q=" + locationName + "&" + dateString + query + "'>" + name(location) + "</a>");
        }
      }
    } catch (Exception e) {
      if (location != null) {
        log.log(Level.INFO, "Error saving location: " + location.getName());
      }
      log.log(Level.INFO, e.getMessage(), e);
    }
    return SKIP_BODY;
  }

  private String name(Location location) {
    return longFormat ? location.getName() : location.getShortenedName();
  }
}
