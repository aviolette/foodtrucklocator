package foodtruck.book;

import com.google.inject.AbstractModule;

/**
 * @author aviolette
 * @since 11/15/16
 */
public class BookingModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(UserService.class).to(UserServiceImpl.class);
  }
}
