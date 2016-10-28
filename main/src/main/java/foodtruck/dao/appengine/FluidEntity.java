package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.Entity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import static foodtruck.dao.appengine.Attributes.getDateTime;
import static foodtruck.dao.appengine.Attributes.getDoubleProperty;
import static foodtruck.dao.appengine.Attributes.getStringProperty;
import static foodtruck.dao.appengine.Attributes.setDateProperty;

/**
 * @author aviolette
 * @since 8/1/16
 */
public class FluidEntity {
  private Entity entity;

  public FluidEntity(Entity entity) {
    this.entity = entity;
  }


  public Entity toEntity() {
    return entity;
  }

  public FluidEntity prop(String label, DateTime dateTime) {
    setDateProperty(label, entity, dateTime);
    return this;
  }

  public FluidEntity prop(String label, Object value) {
    entity.setProperty(label, value);
    return this;
  }

  public double doubleVal(String label) {
    return getDoubleProperty(entity, label, 0);
  }

  public String stringVal(String label) {
    return getStringProperty(entity, label);
  }

  public DateTime dateVal(String label) {
    return getDateTime(entity, label, null);
  }

  public DateTime dateVal(String label, DateTimeZone zone) {
    return getDateTime(entity, label, zone);
  }

  public boolean booleanVal(String propertyName) {
    if (!entity.hasProperty(propertyName)) {
      return false;
    }
    return (Boolean) entity.getProperty(propertyName);
  }

  public long longId() {
    return entity.getKey().getId();
  }
}
