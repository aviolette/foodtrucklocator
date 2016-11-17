package foodtruck.book;

/**
 * @author aviolette
 * @since 11/17/16
 */
public interface PasswordHasher {
  String hash(String password);
}
