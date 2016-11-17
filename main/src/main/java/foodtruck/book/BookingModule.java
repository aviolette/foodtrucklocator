package foodtruck.book;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * @author aviolette
 * @since 11/15/16
 */
public class BookingModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(UserService.class).to(UserServiceImpl.class);
    bind(PasswordHasher.class).to(PasswordHasherImpl.class);
  }

  @Provides
  public HashFunction providesHasher() {
    return Hashing.md5();
  }
}
