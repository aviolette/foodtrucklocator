// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.dao.appengine;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.inject.Inject;

import foodtruck.dao.TruckStopChangeDAO;
import foodtruck.model.TruckStopChange;

/**
 * @author aviolette@gmail.com
 * @since 5/24/12
 */
public class TruckStopChangeDAOAppEngine extends AppEngineDAO<Long, TruckStopChange> implements
    TruckStopChangeDAO {

  private final TruckStopDAOAppEngine truckStopDAO;

  @Inject
  public TruckStopChangeDAOAppEngine(DatastoreServiceProvider provider, TruckStopDAOAppEngine dao) {
    super("TruckStopChange", provider);
    this.truckStopDAO = dao;
  }

  @Override protected Entity toEntity(TruckStopChange obj, Entity entity) {
    EmbeddedEntity to = null;
    if (obj.getTo() != null) {
      to = new EmbeddedEntity();
      truckStopDAO.putProperties(obj.getTo(), to);
    }
    EmbeddedEntity from = new EmbeddedEntity();
    if (obj.getFrom() != null) {
      from = new EmbeddedEntity();
      truckStopDAO.putProperties(obj.getFrom(), from);
    }
    entity.setProperty("timeStamp", obj.getTimeStamp().toDate());
    entity.setProperty("to", to);
    entity.setProperty("from", from);
    return entity;
  }

  @Override protected TruckStopChange fromEntity(Entity entity) {
    return null;
  }
}
