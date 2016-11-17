package foodtruck.book;

import com.google.common.base.Strings;
import com.google.common.hash.HashFunction;
import com.google.inject.Inject;

import org.glassfish.grizzly.http.util.Charsets;

/**
 * @author aviolette
 * @since 11/17/16
 */
class PasswordHasherImpl implements PasswordHasher {
  private final HashFunction hashFunction;

  @Inject
  public PasswordHasherImpl(HashFunction hashFunction) {
    this.hashFunction = hashFunction;
  }

  public String hash(String password) {
    if (Strings.isNullOrEmpty(password)) {
      return "";
    }
    return hashFunction.newHasher()
        .putString(password, Charsets.UTF8_CHARSET)
        .hash()
        .toString();
  }
}
