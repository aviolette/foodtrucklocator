package foodtruck.notifications;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import com.google.api.client.util.Strings;
import com.google.common.io.Closer;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

import foodtruck.notifications.apple.APNSProcessor;
import foodtruck.notifications.email.EmailNotificationProcessor;

/**
 * @author aviolette
 * @since 12/3/12
 */
public class NotificationModule extends AbstractModule {
  private static final Logger log = Logger.getLogger(NotificationModule.class.getName());

  @Override protected void configure() {
    bind(NotificationService.class).to(NotificationServiceImpl.class);
    Multibinder<NotificationProcessor> binder = Multibinder.newSetBinder(binder(), NotificationProcessor.class);
    binder.addBinding().to(EmailNotificationProcessor.class);
    if (Strings.isNullOrEmpty(System.getProperty("foodtrucklocator.apns.password"))) {
      log.info("Disabling push notifications because no cert file is specified");
    } else {
      binder.addBinding().to(APNSProcessor.class);
    }
  }

  @Provides
  public PushNotificationService providePushNotificationService(PushNotificationServiceImpl impl) {
    return impl;
  }

  @Provides @Singleton
  public ApnsService provideApnsService() throws IOException {
    Closer closer = Closer.create();
    try {
      InputStream inputStream = closer.register(getClass().getClassLoader().getResourceAsStream("/resources/cftf.p12"));
      return APNS.newService()
          .withCert(inputStream, System.getProperty("foodtrucklocator.apns.password"))
          .withNoErrorDetection()
          .withSandboxDestination()
          .build();
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }
}
