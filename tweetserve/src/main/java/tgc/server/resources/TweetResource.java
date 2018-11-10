package tgc.server.resources;

import com.google.inject.Inject;
import tgc.db.TweetDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author aviolette
 * @since 11/7/12
 */
@Path("/tweets") @Produces(MediaType.APPLICATION_JSON)
public class TweetResource  {
  private final TweetDAO tweetDAO;

  @Inject
  public TweetResource(TweetDAO tweetDAO) {
    this.tweetDAO = tweetDAO;
  }

  @GET @Path("{from:\\d+}")
  public Response tweetsFromId(@PathParam("from") long fromId) {
    return Response.ok().entity(tweetDAO.findAllFromId(fromId)).build();
  }
}
