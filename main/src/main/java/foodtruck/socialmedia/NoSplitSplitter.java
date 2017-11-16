package foodtruck.socialmedia;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * A splitter that doesn't split anything
 *
 * @author aviolette
 * @since 11/15/17
 */
public class NoSplitSplitter implements MessageSplitter {

  @Override
  public List<String> split(String message) {
    return ImmutableList.of(message);
  }
}
