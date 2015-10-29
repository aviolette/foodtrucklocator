package foodtruck.server.job;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.DailyDataDAO;
import foodtruck.dao.StoryDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.socialmedia.SpecialUpdater;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 10/28/15
 */
@Singleton
public class OneTimeSpecialsSetupServlet extends HttpServlet {
  private final SpecialUpdater specialUpdater;
  private final Clock clock;
  private final StoryDAO storyDAO;
  private final TruckDAO truckDAO;

  @Inject
  public OneTimeSpecialsSetupServlet(DailyDataDAO dailyDataDAO, Clock clock, SpecialUpdater specialUpdater,
      StoryDAO storyDAO, TruckDAO truckDAO) {
    this.clock = clock;
    this.specialUpdater = specialUpdater;
    this.storyDAO = storyDAO;
    this.truckDAO = truckDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    List<Story> stories = storyDAO.findTweetsAfter(clock.currentDay().toDateTimeAtStartOfDay(), "doughnutvault", true);
    Truck truck = truckDAO.findById("thevaultvan");
    specialUpdater.update(truck, stories);

  }
}
