package foodtruck.dao.appengine;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PropertyContainer;
import com.google.appengine.api.datastore.Text;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author aviolette@gmail.com
 * @since 7/3/12
 */
class Attributes {
  public static @Nullable DateTime getDateTime(Entity entity, String propertyName,
      DateTimeZone zone) {
    Date date = (Date) entity.getProperty(propertyName);
    if (date == null) {
      return null;
    }
    return new DateTime(date, zone);
  }

  public static @Nullable String getTextProperty(Entity entity, String propertyName) {
    Text text = (Text) entity.getProperty(propertyName);
    if (text == null) {
      return null;
    }
    return text.getValue();
  }

  public static String getStringProperty(Entity entity, String propertyName) {
    return (String) entity.getProperty(propertyName);
  }

  public static void setDateProperty(String propertyName, PropertyContainer entity,
      @Nullable DateTime dateTime) {
    if (dateTime == null) {
      entity.setProperty(propertyName, null);
    } else {
      entity.setProperty(propertyName, dateTime.toDate());
    }
  }

  public static int getIntProperty(Entity entity, String statName, int defaultValue) {
    if (entity.hasProperty(statName)) {
      Number num = (Number) entity.getProperty(statName);
      return num.intValue();
    }
    return defaultValue;
  }

  public static long getLongProperty(Entity entity, String statName, long defaultValue) {
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

  public static List<String> getListProperty(Entity entity, String name) {
    if (entity.hasProperty(name)) {
      List<String> val = (List<String>) entity.getProperty(name);
      return val == null ? ImmutableList.<String>of() : val;
    }
    return ImmutableList.of();
  }

  public static boolean getBooleanProperty(Entity entity, String statName, boolean defaultValue) {
    if (entity.hasProperty(statName)) {
      return (Boolean) entity.getProperty(statName);
    }
    return defaultValue;
  }

  public static Set<String> getSetProperty(Entity entity, String name) {
    if (entity.hasProperty(name)) {
      Collection<String> values = (Collection<String>) entity.getProperty(name);
      if (values == null) {
        return ImmutableSet.of();
      }
      return ImmutableSet.copyOf(values);
    }
    return ImmutableSet.of();
  }
}
