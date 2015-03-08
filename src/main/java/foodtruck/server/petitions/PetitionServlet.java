package foodtruck.server.petitions;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.PetitionSignatureDAO;
import foodtruck.email.EmailNotifier;
import foodtruck.model.PetitionSignature;
import foodtruck.model.StaticConfig;
import foodtruck.server.FrontPageServlet;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 7/25/14
 */
@Singleton
public class PetitionServlet extends FrontPageServlet {
  private static final double GOAL = 1000;
  private static final String JSP = "/WEB-INF/jsp/petitions/600w.jsp";
  private final PetitionSignatureDAO petitionDAO;
  private final Clock clock;
  private final EmailNotifier notifier;
  private SecureRandom random = new SecureRandom();

  @Inject
  public PetitionServlet(PetitionSignatureDAO petitionDAO, Clock clock,
      EmailNotifier notifier, StaticConfig staticConfig) {
    super(staticConfig);
    this.petitionDAO = petitionDAO;
    this.clock = clock;
    this.notifier = notifier;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    long totalSignatures = petitionDAO.findSigned("600w");
    req.setAttribute("numSignatures", totalSignatures);
    double percentage = Math.floor(((double)totalSignatures / GOAL) * 100);
    if (percentage > 100) {
      percentage = 100;
    }
    if (percentage < 5) {
      percentage = 5;
    }
    req.setAttribute("signaturePercentages", (int)Math.floor(percentage));
    req.setAttribute("title", "Petition for Food Truck Stands at 600 West Chicago");
    req.getRequestDispatcher(JSP).forward(req, resp);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final Escaper escaper = HtmlEscapers.htmlEscaper();
    final String email = escaper.escape(Strings.nullToEmpty(req.getParameter("email")));
    PetitionSignature sig = petitionDAO.findSignedByEmail(email);
    if (sig != null) {
      flashError("A person with that email has already signed this petition", resp);
      resp.sendRedirect("/petitions/600w");
      return;
    }
    sig = PetitionSignature.builder()
        .created(clock.now())
        .email(email)
        .inWard("on".equals(req.getParameter("inWard")))
        .lastName(escaper.escape(Strings.nullToEmpty(req.getParameter("lastName"))))
        .firstName(escaper.escape(Strings.nullToEmpty(req.getParameter("firstName"))))
        .petitionId("600w")
        .signature(new BigInteger(130, random).toString(32))
        .zipcode(escaper.escape(Strings.nullToEmpty(req.getParameter("zipcode"))))
        .build();
    try {
      sig.validate();
    } catch (IllegalStateException ex) {
      flashError(ex.getMessage(), resp);
      resp.sendRedirect("/petitions/600w");
      return;
    }
    petitionDAO.save(sig);
    notifier.notifyVerifyPetitionSignature(sig);
    resp.sendRedirect("/petitions/600w/not_finished");
  }
}
