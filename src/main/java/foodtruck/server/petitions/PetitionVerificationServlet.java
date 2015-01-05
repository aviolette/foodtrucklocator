package foodtruck.server.petitions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.PetitionSignatureDAO;
import foodtruck.email.EmailNotifier;
import foodtruck.model.PetitionSignature;
import foodtruck.model.StaticConfig;
import foodtruck.server.FrontPageServlet;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 7/29/14
 */
@Singleton
public class PetitionVerificationServlet extends FrontPageServlet {
  private final PetitionSignatureDAO petitionSignatureDAO;
  private final Clock clock;
  private final EmailNotifier notifier;

  @Inject
  public PetitionVerificationServlet(ConfigurationDAO configDAO, PetitionSignatureDAO petitionSignatureDAO,
      Clock clock, EmailNotifier notifier, StaticConfig staticConfig) {
    super(configDAO, staticConfig);
    this.petitionSignatureDAO = petitionSignatureDAO;
    this.clock = clock;
    this.notifier = notifier;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String signature = req.getRequestURI().substring("/petitions/600w/verify/".length());
    PetitionSignature sig = petitionSignatureDAO.findBySignature(signature);
    if (sig == null) {
      flashError("Your name has already been submitted.  Thanks!", resp);
      resp.sendRedirect("/petitions/600w");
      return;
    }
    petitionSignatureDAO.save(PetitionSignature.builder(sig).signed(clock.now()).build());
    notifier.notifyThanksForSigningPetition(sig);
    resp.sendRedirect("/petitions/600w/thanks");
  }
}
