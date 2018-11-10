package tgc.db;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import java.net.UnknownHostException;

/**
 * @author aviolette
 * @since 11/7/12
 */
public class DBModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(TweetDAO.class).to(TweetDAOMongo.class);
  }

  @Provides
  public DBCollection provideTweets() {
    DBAddress address;
    try {
      address = new DBAddress(System.getProperty("tgc.db.address"));
    } catch (UnknownHostException e) {
      throw Throwables.propagate(e);
    }
    DB db = Mongo.connect(address);
    return db.getCollection("tweets");
  }
}
