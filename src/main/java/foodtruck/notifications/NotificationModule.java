package foodtruck.notifications;

import com.google.inject.AbstractModule;

/**
 * @author aviolette
 * @since 12/3/12
 */
public class NotificationModule extends AbstractModule {

  @Override protected void configure() {
    bind(NotificationService.class).to(NotificationServiceImpl.class);
  }
}
