package foodtruck.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.joda.time.LocalDate;

import foodtruck.dao.DailyDataDAO;
import foodtruck.model.DailyData;
import foodtruck.model.Truck;
import foodtruck.time.Clock;

import static foodtruck.server.resources.Resources.requiresAdmin;

/**
 * @author aviolette
 * @since 4/14/16
 */
@Produces("application/json")
public class DailySpecialResource {
  private final Truck truck;
  private final DailyDataDAO dailyDataDAO;
  private final Clock clock;

  @Inject
  public DailySpecialResource(Clock clock, DailyDataDAO dailyDataDAO, @Assisted Truck truck) {
    this.truck = truck;
    this.dailyDataDAO = dailyDataDAO;
    this.clock = clock;
  }

  @GET
  public DailyData findForDate(@QueryParam("date") String dateString) {
    LocalDate date = clock.currentDay();
    DailyData dailyData =  dailyDataDAO.findByTruckAndDay(truck.getId(), clock.currentDay());
    if (dailyData == null) {
      return DailyData.builder().onDate(date).truckId(truck.getId()).build();
    }
    return dailyData;
  }

  @PUT
  public void save(DailyData dailyData) {
    requiresAdmin();
    if (dailyData.hasSpecials()) {
      dailyDataDAO.save(dailyData);
    } else if (!dailyData.isNew()) {
      dailyDataDAO.delete((Long) dailyData.getKey());
    }
  }
}
