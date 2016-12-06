package foodtruck.dao.ws;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONArray;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 12/5/16
 */
public class TruckDAOWebClient implements TruckDAO {
  private final WebResource resource;


  @Inject
  public TruckDAOWebClient(@Named("dao") WebResource resource) {
    this.resource = resource;
  }

  @Override
  public Collection<Truck> findByTwitterId(String twitterHandle) {
    JSONArray arr = resource.queryParam("twitterhandle", twitterHandle)
        .path("/v1/services/trucks")
        .get(JSONArray.class);
    return null;
  }

  @Override
  public List<Truck> findActiveTrucks() {
    return null;
  }

  @Override
  public Collection<Truck> findInactiveTrucks() {
    return null;
  }

  @Override
  public Set<Truck> findTrucksWithCalendars() {
    return null;
  }

  @Override
  public List<Truck> findVisibleTrucks() {
    return null;
  }

  @Override
  public List<Truck> findFacebookTrucks() {
    return null;
  }

  @Nullable
  @Override
  public Truck findFirst() {
    return null;
  }

  @Override
  public List<Truck> findByCategory(String s) {
    return null;
  }

  @Override
  public Set<Truck> findByBeaconnaiseEmail(String s) {
    return null;
  }

  @Override
  public Iterable<Truck> findTrucksWithEmail() {
    return null;
  }

  @Override
  public void deleteAll() {

  }

  @Nullable
  @Override
  public Truck findByName(String s) {
    return null;
  }

  @Nullable
  @Override
  public Truck findByNameOrAlias(String s) {
    return null;
  }

  @Override
  public List<Truck> findAll() {
    return null;
  }

  @Override
  public long save(Truck truck) {
    return 0;
  }

  @Nullable
  @Override
  public Truck findById(String s) {
    return null;
  }

  @Override
  public void delete(String s) {

  }

  @Override
  public long count() {
    return 0;
  }
}
