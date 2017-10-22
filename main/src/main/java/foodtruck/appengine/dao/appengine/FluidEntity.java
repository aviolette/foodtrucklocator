package foodtruck.appengine.dao.appengine;

import com.google.appengine.api.datastore.Entity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @author aviolette
 * @since 8/1/16
 */
public class FluidEntity {
  private Entity entity;

  FluidEntity(Entity entity) {
    this.entity = entity;
  }


  public Entity toEntity() {
    return entity;
  }

  public FluidEntity prop(String label, DateTime dateTime) {
    Attributes.setDateProperty(label, entity, dateTime);
    return this;
  }

  public FluidEntity prop(String label, Object value) {
    entity.setProperty(label, value);
    return this;
  }

  double doubleVal(String label) {
    return Attributes.getDoubleProperty(entity, label, 0);
  }

  String stringVal(String label) {
    return Attributes.getStringProperty(entity, label);
  }

  DateTime dateVal(String label) {
    return Attributes.getDateTime(entity, label, null);
  }

  DateTime dateVal(String label, DateTimeZone zone) {
    return Attributes.getDateTime(entity, label, zone);
  }

  boolean booleanVal(String propertyName) {
    if (!entity.hasProperty(propertyName)) {
      return false;
    }
    return (Boolean) entity.getProperty(propertyName);
  }

  long longId() {
    return entity.getKey().getId();
  }

  int intValue(String propertyName) {
    return Attributes.getIntProperty(entity, propertyName, 0);
  }
}
