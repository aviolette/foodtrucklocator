package foodtruck.server.dashboard.truck;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.util.Link;

/**
 * @author aviolette
 * @since 11/23/16
 */
@Singleton
public class TruckConfigurationServlet extends AbstractTruckServlet {
  private static final Splitter BLACK_LIST_SPLITTER = Splitter.on(";")
      .omitEmptyStrings()
      .trimResults();
  private static final String JSP = "/WEB-INF/jsp/dashboard/truck/truckEdit.jsp";

  @Inject
  public TruckConfigurationServlet(TruckDAO truckDAO) {
    super(truckDAO);
  }

  @Override
  protected void doGetProtected(HttpServletRequest request, HttpServletResponse response,
      Truck truck) throws ServletException, IOException {
    request.setAttribute("headerName", "Edit");
    request.setAttribute("headerSelection", "config");
    request.setAttribute("extraScripts", ImmutableList.of("/script/dashboard-truck-config.js", "/script/flash.js"));
    forward(request, response);
  }

  @Override
  protected void doPostProtected(HttpServletRequest request, HttpServletResponse response,
      Truck truck) throws IOException {
    String contentType = request.getContentType();
    if (MediaType.APPLICATION_FORM_URLENCODED.equals(contentType)) {
      Truck theTruck = truckFromForm(request, truck);
      truckDAO.save(theTruck);
      response.sendRedirect("/admin/trucks/" + truck.getId() + "/configuration");
    }
  }

  @Override
  protected ImmutableList<Link> breadcrumbs(Truck truck) {
    return ImmutableList.of(new Link("Trucks", "/admin/trucks"),
        new Link(truck.getName(), "/admin/trucks/" + truck.getId()),
        new Link("Edit", "/admin/trucks/" + truck.getId() + "/configuration"));
  }

  @Override
  protected String getJsp() {
    return JSP;
  }

  private Truck truckFromForm(HttpServletRequest request, Truck truck) {
    Truck.Builder builder = Truck.builder(truck);
    builder.id(truck.getId())
        .defaultCity(request.getParameter("defaultCity"))
        .description(request.getParameter("description"))
        .calendarUrl(request.getParameter("calendarUrl"))
        .phoneticMarkup(request.getParameter("phoneticMarkup"))
        .menuUrl(request.getParameter("menuUrl"))
        .previewIcon(request.getParameter("previewIcon"))
        .fullsizeImage(request.getParameter("fullsizeImage"))
        .instagramId(request.getParameter("instagramId"))
        .facebook(request.getParameter("facebook"))
        .facebookPageId(request.getParameter("facebookPageId"))
        .foursquareUrl(request.getParameter("foursquareUrl"))
        .drupalCalendar(request.getParameter("drupalCalendar"))
        .icalCalendar(request.getParameter("iCalCalendar"))
        .squarespaceCalendar(request.getParameter("squarespaceCalendar"))
        .iconUrl(request.getParameter("iconUrl"))
        .backgroundImage(request.getParameter("backgroundImage"))
        .backgroundImageLarge(request.getParameter("backgroundImageLarge"))
        .timezoneOffset(Integer.parseInt(request.getParameter("timezoneAdjustment")))
        .name(request.getParameter("name"))
        .yelpSlug(request.getParameter("yelp"))
        .normalizePhone(request.getParameter("phone"))
        .email(request.getParameter("email"))
        .twitterHandle(request.getParameter("twitterHandle"))
        .url(request.getParameter("url"));
    final String[] optionsArray = request.getParameterValues("options");
    Set<String> options = ImmutableSet.copyOf(optionsArray == null ? new String[0] : optionsArray);
    builder.inactive(options.contains("inactive"));
    builder.hidden(options.contains("hidden"));
    builder.useTwittalyzer(options.contains("twittalyzer"));
    builder.notifyOfLocationChanges(options.contains("notifyOfLocationChanges"));
    builder.scanFacebook(options.contains("facebooker"));
    builder.displayEmailPublicly(options.contains("displayEmailPublicly"));
    String matchRegex = request.getParameter("matchOnlyIf");
    builder.matchOnlyIf(Strings.isNullOrEmpty(matchRegex) ? null : matchRegex);
    String doNotMatchRegex = request.getParameter("donotMatchIf");
    builder.donotMatchIf(Strings.isNullOrEmpty(doNotMatchRegex) ? null : doNotMatchRegex);
    Splitter splitter = Splitter.on(",")
        .omitEmptyStrings()
        .trimResults();
    builder.categories(ImmutableSet.copyOf(splitter.split(request.getParameter("categories"))));
    builder.beaconnaiseEmails(ImmutableSet.copyOf(splitter.split(request.getParameter("beaconnaiseEmails"))));
    ImmutableList.Builder<String> aliases = ImmutableList.builder();
    for (String item : splitter.split(request.getParameter("phoneticAliases"))) {
      aliases.add(item.toLowerCase());
    }
    builder.phoneticAliases(aliases.build());
    builder.blacklistLocationNames(BLACK_LIST_SPLITTER.splitToList(request.getParameter("blacklistLocations")));
    try {
      builder.fleetSize(Integer.parseInt(request.getParameter("fleetSize")));
    } catch (NumberFormatException ignored) {
    }
    return builder.build();
  }
}
