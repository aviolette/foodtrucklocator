package foodtruck.appengine.dao.appengine;

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

import foodtruck.model.Url;

/**
 * @author aviolette@gmail.com
 * @since 7/3/12
 */
class Attributes {
  static @Nullable DateTime getDateTime(Entity entity, String propertyName, DateTimeZone zone) {
    Date date = (Date) entity.getProperty(propertyName);
    if (date == null) {
      return null;
    }
    return new DateTime(date, zone);
  }

  static @Nullable String getTextProperty(Entity entity, String propertyName) {
    Text text = (Text) entity.getProperty(propertyName);
    if (text == null) {
      return null;
    }
    return text.getValue();
  }

  static String getStringProperty(Entity entity, String propertyName) {
    return (String) entity.getProperty(propertyName);
  }

  static String getStringProperty(Entity entity, String name, String defaultValue) {
    String value = getStringProperty(entity, name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  static Url getUrlProperty(Entity entity, String propertyName) {
    String value = getStringProperty(entity, propertyName);
    if (value == null) {
      return null;
    }
    return new Url(value);
  }

  static void setUrlProperty(Entity entity, String propertyName, @Nullable Url value) {
    if (value == null) {
      entity.setProperty(propertyName, null);
    } else {
      entity.setProperty(propertyName, value.getUrl());
    }
  }

  static void setDateProperty(String propertyName, PropertyContainer entity, @Nullable DateTime dateTime) {
    if (dateTime == null) {
      entity.setProperty(propertyName, null);
    } else {
      entity.setProperty(propertyName, dateTime.toDate());
    }
  }

  static int getIntProperty(Entity entity, String statName, int defaultValue) {
    if (entity.hasProperty(statName)) {
      Number num = (Number) entity.getProperty(statName);
      return num.intValue();
    }
    return defaultValue;
  }

  static long getLongProperty(Entity entity, String statName, long defaultValue) {
    if (entity.hasProperty(statName)) {
      return (Long) entity.getProperty(statName);
    }
    return defaultValue;
  }

  static double getDoubleProperty(Entity entity, String statName, double defaultValue) {
    if (entity.hasProperty(statName)) {
      return (Double) entity.getProperty(statName);
    }
    return defaultValue;
  }

  @SuppressWarnings("unchecked")
  static List<String> getListProperty(Entity entity, String name) {
    if (entity.hasProperty(name)) {
      List<String> val = (List<String>) entity.getProperty(name);
      return val == null ? ImmutableList.<String>of() : val;
    }
    return ImmutableList.of();
  }

  @SuppressWarnings("unchecked")
  static Set<String> getSetProperty(Entity entity, String name) {
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
