// Copyright 2010 BrightTag, Inc. All rights reserved.
package foodtruck.dao.appengine;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since 2/26/12
 */
public class TruckDAOAppEngine implements TruckDAO {
  private final DatastoreServiceProvider provider;
  private static final String TRUCK_KIND = "Store";
  private static final String TRUCK_ID_FIELD = "id";
  private static final String TRUCK_NAME_FIELD = "name";
  private static final String TRUCK_TWITTER_HANDLE = "twitterHandle";
  private static final String TRUCK_URL = "url";
  private static final String TRUCK_ICON_URL = "iconUrl";
  private static final String TRUCK_DESCRIPTION_FIELD = "description";
  private static final String TRUCK_FOURSQUARE_URL_FIELD = "foursquareUrl";
  private static final String TRUCK_TWITTALYZER_FIELD = "useTwittalyzer";
  private static final String TRUCK_DEFAULT_CITY_FIELD = "defaultCity";
  private static final String TRUCK_FACEBOOK_FIELD = "facebookUrl";
  private static final String MATCH_REGEX_FIELD = "matchOnlyIf";
  private static final String INACTIVE_FIELD = "inactive";
  private static final String CATEGORIES_FIELD = "categories";
  private static final String TRUCK_CALENDAR_URL = "calendarUrl";

  @Inject
  public TruckDAOAppEngine(DatastoreServiceProvider provider) {
    this.provider = provider;
  }

  @Override public Collection<Truck> findAll() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    ImmutableSet.Builder<Truck> trucks = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      Truck truck = fromEntity(entity);
      trucks.add(truck);
    }
    return trucks.build();
  }

  private Truck fromEntity(Entity entity) {
    Truck.Builder builder = Truck.builder();
    Collection categoriesList = (Collection) entity.getProperty(CATEGORIES_FIELD);
    return builder.id(entity.getKey().getName())
        .key(entity.getKey())
        .inactive((Boolean) entity.getProperty(INACTIVE_FIELD))
        .twitterHandle((String) entity.getProperty(TRUCK_TWITTER_HANDLE))
        .defaultCity((String) entity.getProperty(TRUCK_DEFAULT_CITY_FIELD))
        .description((String) entity.getProperty(TRUCK_DESCRIPTION_FIELD))
        .facebook((String) entity.getProperty(TRUCK_FACEBOOK_FIELD))
        .foursquareUrl((String) entity.getProperty(TRUCK_FOURSQUARE_URL_FIELD))
        .iconUrl((String) entity.getProperty(TRUCK_ICON_URL))
        .name((String) entity.getProperty(TRUCK_NAME_FIELD))
        .matchOnlyIf((String) entity.getProperty(MATCH_REGEX_FIELD))
        .url((String) entity.getProperty(TRUCK_URL))
        .categories(categoriesList == null ? ImmutableSet.<String>of() :
            ImmutableSet.copyOf(categoriesList))
        .useTwittalyzer((Boolean) entity.getProperty(TRUCK_TWITTALYZER_FIELD))
        .calendarUrl((String) entity.getProperty(TRUCK_CALENDAR_URL))
        .build();
  }

  @Override public @Nullable Truck findById(String id) {
    DatastoreService dataStore = provider.get();
    try {
      return fromEntity(dataStore.get(KeyFactory.createKey(TRUCK_KIND, id)));
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  @Override public void save(Truck truck) {
    DatastoreService dataStore = provider.get();
    Entity entity = null;
    try {
      if (!truck.isNew()) {
        Key key = KeyFactory.createKey(TRUCK_KIND, truck.getId());
        entity = dataStore.get(key);
      }
      entity = toEntity(truck, entity);
      dataStore.put(entity);
    } catch (EntityNotFoundException e) {
      throw new RuntimeException(e);
    }

  }

  @Override public Collection<Truck> findByTwitterId(String screenName) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    q.addFilter(TRUCK_TWITTER_HANDLE, Query.FilterOperator.EQUAL, screenName);
    ImmutableSet.Builder<Truck> trucks = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      Truck truck = fromEntity(entity);
      trucks.add(truck);
    }
    return trucks.build();
  }

  @Override public Collection<Truck> findAllTwitterTrucks() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    q.addFilter(TRUCK_TWITTALYZER_FIELD, Query.FilterOperator.EQUAL, true);
    ImmutableSet.Builder<Truck> trucks = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      Truck truck = fromEntity(entity);
      trucks.add(truck);
    }
    return trucks.build();
  }

  @Override public Set<Truck> findTrucksWithCalendars() {
    DatastoreService dataStore = provider.get();
    Query q = new Query(TRUCK_KIND);
    q.addFilter(TRUCK_CALENDAR_URL, Query.FilterOperator.NOT_EQUAL, null);
    ImmutableSet.Builder<Truck> trucks = ImmutableSet.builder();
    for (Entity entity : dataStore.prepare(q).asIterable()) {
      Truck truck = fromEntity(entity);
      trucks.add(truck);
    }
    return trucks.build();
  }

  private Entity toEntity(Truck truck, Entity entity) {
    Entity truckEntity = entity == null ? new Entity(TRUCK_KIND, truck.getId()) : entity;
    truckEntity.setProperty(TRUCK_NAME_FIELD, truck.getName());
    truckEntity.setProperty(TRUCK_TWITTER_HANDLE, truck.getTwitterHandle());
    truckEntity.setProperty(TRUCK_URL, truck.getUrl());
    truckEntity.setProperty(TRUCK_ICON_URL, truck.getIconUrl());
    truckEntity.setProperty(TRUCK_CALENDAR_URL,
        Strings.isNullOrEmpty(truck.getCalendarUrl()) ? null : truck.getCalendarUrl());
    truckEntity.setProperty(TRUCK_DESCRIPTION_FIELD, truck.getDescription());
    truckEntity.setProperty(TRUCK_FOURSQUARE_URL_FIELD, truck.getFoursquareUrl());
    truckEntity.setProperty(TRUCK_TWITTALYZER_FIELD, truck.isUsingTwittalyzer());
    truckEntity.setProperty(TRUCK_DEFAULT_CITY_FIELD, truck.getDefaultCity());
    truckEntity.setProperty(TRUCK_FACEBOOK_FIELD, truck.getFacebook());
    truckEntity.setProperty(MATCH_REGEX_FIELD, truck.getMatchOnlyIfString());
    truckEntity.setProperty(INACTIVE_FIELD, truck.isInactive());
    truckEntity.setProperty(CATEGORIES_FIELD, truck.getCategories());
    return truckEntity;
  }
}
