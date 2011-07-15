package net.andrewviolette.foodtruck.dao.stub;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;

import net.andrewviolette.foodtruck.dao.TruckStopDAO;
import net.andrewviolette.foodtruck.model.Location;
import net.andrewviolette.foodtruck.model.Truck;
import net.andrewviolette.foodtruck.model.TruckStop;

/**
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
public class TruckStopDAOStub implements TruckStopDAO {
  @Override
  public Set<TruckStop> findAfter(DateTime instant) {
    Truck tamaleSpaceship = new Truck("tamalespaceship", "Tamale Spaceship", null, null,
        "https://si0.twimg.com/profile_images/1402134609/be86_1_1__normal.jpg");
    TruckStop truckStop = new TruckStop(tamaleSpaceship, new DateTime().minusHours(3),
        new DateTime().plusHours(3), new Location(41.8857044, -87.6413047));
    return ImmutableSet.of(truckStop);
  }

  @Override
  public void deleteAfter(DateTime startTime) {

  }

  @Override
  public void addStops(List<TruckStop> stops) {

  }
}
