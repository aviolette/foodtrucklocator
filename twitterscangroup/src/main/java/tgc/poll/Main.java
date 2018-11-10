package tgc.poll;

import com.google.common.base.Throwables;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.Mongo;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author aviolette
 * @since 11/5/12
 */
public class Main {
  public static final Logger log = Logger.getLogger(Main.class.getName());
  public static void main(String args[]) {
    if (args.length != 2) {
      System.err.println("Usage: tgc.poll.Main <dbUrl> <listId>");
      System.exit(-1);
    }
    // Initialize twitter client
    TwitterFactory factory = new TwitterFactory();
    Twitter twitter = factory.getInstance();
    // Initialize mongo connections
    DBAddress address;
    try {
      address = new DBAddress(args[0]);
    } catch (UnknownHostException e) {
      throw Throwables.propagate(e);
    }
    DB db = Mongo.connect(address);
    int twitterListId = Integer.parseInt(args[1]);
    log.log(Level.INFO, "Polling collection {1} and using DB: {0}", args);
    new Poller(db.getCollection("config"), db.getCollection("tweets"), twitter, twitterListId).run();
  }
}
