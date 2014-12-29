package foodtruck.server.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.api.client.util.ByteStreams;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.sun.jersey.api.JResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;
import foodtruck.twitter.TwitterFactoryWrapper;
import foodtruck.util.Clock;
import foodtruck.util.DateOnlyFormatter;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import static foodtruck.server.resources.Resources.requiresAdmin;

/**
 * @author aviolette@gmail.com
 * @since 6/13/12
 */
@Path("/trucks{view : (\\.[a-z]{3})?}")
public class TruckResource {
  private static final Logger log = Logger.getLogger(TruckResource.class.getName());
  public static final Predicate<Truck> NOT_HIDDEN = new Predicate<Truck>() {
    @Override public boolean apply(@Nullable Truck truck) {
      return !truck.isHidden();
    }
  };
  private final TruckDAO truckDAO;
  private final Clock clock;
  private final DateTimeZone zone;
  private final DateTimeFormatter formatter;
  private final TwitterFactoryWrapper twitterFactory;
  private final GcsService cloudStorage;

  @Inject
  public TruckResource(TruckDAO truckDAO, Clock clock, DateTimeZone zone, @DateOnlyFormatter DateTimeFormatter formatter,
      TwitterFactoryWrapper twitterFactory, GcsService cloudStorage) {
    this.truckDAO = truckDAO;
    this.clock = clock;
    this.zone = zone;
    this.formatter = formatter;
    this.twitterFactory = twitterFactory;
    this.cloudStorage = cloudStorage;
  }

  @GET
  @Produces({"application/json", "text/csv"})
  public JResponse<Collection<Truck>> getTrucks(@PathParam("view") String view, @QueryParam("active") final String active,
      @QueryParam("tag") final String filteredBy) {
    MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
    if (".csv".equals(view)) {
      mediaType = new MediaType("text", "csv");
    }
    Collection<Truck> response;
    if ("false".equals(active)) {
      response = truckDAO.findInactiveTrucks();
    } else {
      response = Strings.isNullOrEmpty(filteredBy) ? truckDAO.findActiveTrucks() : truckDAO.findByCategory(filteredBy);
    }
    return JResponse.ok(Collections2.filter(response, NOT_HIDDEN), mediaType).build();
  }

  @GET @Produces("application/json") @Path("{truckId}")
  public JResponse<Truck> getTruck(@PathParam("truckId") String truckId) {
    Truck t = truckDAO  .findById(truckId);
    return JResponse.ok(t).build();
  }

  @POST @Path("{truckId}/mute")
  public void muteTruck(@PathParam("truckId") String truckId, @QueryParam("until") String until) {
    requiresAdmin();
    DateTime muteUntil = Strings.isNullOrEmpty(until) ?
        clock.currentDay().toDateMidnight(zone).toDateTime().plusDays(1) :
        formatter.parseDateTime(until);
    Truck t = truckDAO.findById(truckId);
    t = Truck.builder(t).muteUntil(muteUntil)
        .build();
    truckDAO.save(t);
  }

  @POST @Path("{truckId}/unmute")
  public void unmuteTruck(@PathParam("truckId") String truckId) {
    requiresAdmin();
    Truck t = truckDAO.findById(truckId);
    t = Truck.builder(t).muteUntil(null)
        .build();
    truckDAO.save(t);
  }

  @DELETE @Path("{truckId}")
  public void delete(@PathParam("truckId") String truckId) {
    truckDAO.delete(truckId);
  }

  @POST @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
  public JResponse<Truck> createTruck(Truck truck) {
    Resources.requiresAdmin();
    if (truckDAO.findById(truck.getId()) != null) {
      throw new BadRequestException("POST can only be used , for creating objects");
    }

    Twitter twitter = twitterFactory.create();
    try {
      ResponseList<User> lookup = twitter.users().lookupUsers(new String[]{truck.getTwitterHandle()});
      User user = Iterables.getFirst(lookup, null);
      if (user != null) {
        String url = user.getProfileImageURL();
        try {
          String extension = url.substring(url.lastIndexOf(".")),
              fileName = truck.getTwitterHandle() + extension;
          GcsFilename gcsFilename = new GcsFilename("truckicons", fileName);
          GcsOutputChannel channel = cloudStorage.createOrReplace(gcsFilename,
              GcsFileOptions.getDefaultInstance());
          URL iconUrl = new URL(url);
          InputStream in = iconUrl.openStream();
          OutputStream out = Channels.newOutputStream(channel);
          try {
            ByteStreams.copy(in, out);
          } finally {
            in.close();
            out.close();
          }
        } catch (IOException io) {
          log.log(Level.WARNING, io.getMessage(), io);
        }
        truck = Truck.builder(truck)
            .name(user.getName())
            .iconUrl(url)
            .build();
      }
    } catch (TwitterException e) {
      log.log(Level.WARNING, "Error contacting twitter", e.getMessage());
    }
    truckDAO.save(truck);
    return JResponse.ok(truck).build();
  }
}
