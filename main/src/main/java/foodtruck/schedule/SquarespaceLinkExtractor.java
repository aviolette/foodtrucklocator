package foodtruck.schedule;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import foodtruck.util.Urls;

/**
 * @author aviolette
 * @since 10/16/18
 */
public class SquarespaceLinkExtractor {

  public List<String> findLinks(String document, String baseUrl) throws MalformedURLException {

    URL url = new URL(Objects.requireNonNull(baseUrl));
    String path = url.getPath() + "/";
    String basePath = Urls.baseUrl(url);
    Document doc = Jsoup.parse(document);
    Elements links = doc.select("a");
    ImmutableList.Builder<String> builder = ImmutableList.builder();

    for (Element link : links) {
      String uri = link.attr("href");
      if (uri.startsWith(path) && !uri.equals(path)) {
        String icalUrl = basePath + uri + "?format=ical";
        builder.add(icalUrl);
      }
    }
    return builder.build();
  }
}
