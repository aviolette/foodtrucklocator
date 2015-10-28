package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.SpecialsDAO;
import foodtruck.model.Specials;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 10/28/15
 */
@Singleton
public class OneTimeSpecialsSetupServlet extends HttpServlet {
  private final SpecialsDAO specialsDAO;
  private final Clock clock;

  @Inject
  public OneTimeSpecialsSetupServlet(SpecialsDAO specialsDAO, Clock clock) {
    this.specialsDAO = specialsDAO;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Specials specials = specialsDAO.findByLocationAndDay("Doughnut Vault @ Canal", clock.currentDay());
    if (specials == null) {
      specials = Specials.builder()
          .addSpecial("Chestnut old-fashioned", false)
          .onDate(clock.currentDay())
          .locationId("Doughnut Vault @ Canal")
          .build();
      specialsDAO.save(specials);
    }

  }
}
