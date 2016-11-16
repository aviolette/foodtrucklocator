package foodtruck.provisioning;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.common.base.Throwables;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.UserList;
import twitter4j.conf.PropertyConfiguration;

/**
 * An app to find the slugs for the twitter lists
 *
 * @author aviolette
 * @since 10/3/16
 */
@SuppressWarnings("AppEngineForbiddenCode")
public class ListList {
  public static void main(String args[]) throws Exception {
    Properties properties = new Properties();
    InputStream in = Thread.currentThread()
        .getContextClassLoader()
        .getResourceAsStream("twitter4j.properties");
    try {
      properties.load(in);
      in.close();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    Twitter twitter = new TwitterFactory(new PropertyConfiguration(properties)).getInstance();
    for (UserList userList : twitter.list()
        .getUserLists("chifoodtruckz")) {
      System.out.println(userList.getName() + " " + userList.getSlug());
    }
    System.exit(0);
  }

}
