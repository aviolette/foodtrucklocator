package foodtruck.dao.appengine;

import java.util.Date;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.Entity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author aviolette@gmail.com
 * @since 7/3/12
 */
public class Attributes {
  public static @Nullable DateTime getDateTime(Entity entity, String propertyName,
      DateTimeZone zone) {
    Date date = (Date) entity.getProperty(propertyName);
    if (date == null) {
      return null;
    }
    return new DateTime(date, zone);
  }

  public static String getStringProperty(Entity entity, String propertyName) {
    return (String)entity.getProperty(propertyName);
  }

  public static void setDateProperty(String propertyName, Entity entity,
      @Nullable DateTime dateTime) {
    if (dateTime == null) {
      entity.setProperty(propertyName, null);
    } else {
      entity.setProperty(propertyName, dateTime.toDate());
    }
  }

  public static long getLongProperty(Entity entity, String statName, int defaultValue) {
    if (entity.hasProperty(statName)) {
      return (Long) entity.getProperty(statName);
    }
    return defaultValue;
  }

  public static double getDoubleProperty(Entity entity, String statName, double defaultValue) {
    if (entity.hasProperty(statName)) {
      return (Double) entity.getProperty(statName);
    }
    return defaultValue;
  }

  public static boolean getBooleanProperty(Entity entity, String statName) {
    return getBooleanProperty(entity, statName, false);
  }

  public static boolean getBooleanProperty(Entity entity, String statName, boolean defaultValue) {
    if (entity.hasProperty(statName)) {
      return (Boolean) entity.getProperty(statName);
    }
    return defaultValue;
  }
}
