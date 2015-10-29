package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.DailyDataDAO;
import foodtruck.model.DailyData;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 10/28/15
 */
@Singleton
public class OneTimeSpecialsSetupServlet extends HttpServlet {
  private final DailyDataDAO dailyDataDAO;
  private final Clock clock;

  @Inject
  public OneTimeSpecialsSetupServlet(DailyDataDAO dailyDataDAO, Clock clock) {
    this.dailyDataDAO = dailyDataDAO;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    DailyData dailyData = dailyDataDAO.findByLocationAndDay("Doughnut Vault @ Canal", clock.currentDay());
    if (dailyData == null) {
      dailyData = DailyData.builder()
          .addSpecial("Chestnut old-fashioned", false)
          .onDate(clock.currentDay())
          .locationId("Doughnut Vault @ Canal")
          .build();
      dailyDataDAO.save(dailyData);
    }

  }
}
