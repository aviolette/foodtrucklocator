package foodtruck.notifications;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;

/**
 * @author aviolette
 * @since 12/3/12
 */
public class NotificationModule extends AbstractModule {
  @Override protected void configure() {
    bind(NotificationService.class).to(NotificationServiceImpl.class);
    Multibinder<NotificationProcessor> binder = Multibinder.newSetBinder(binder(), NotificationProcessor.class);
    binder.addBinding().to(EmailNotificationProcessor.class);
  }

  @Provides
  public PushNotificationService providePushNotificationService(PushNotificationServiceImpl impl) {
    return impl;
  }
}
