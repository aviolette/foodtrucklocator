package foodtruck.mail;

import com.google.inject.AbstractModule;

/**
 * @author aviolette
 * @since 12/5/16
 */
public class MailModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SystemNotificationService.class).to(SimpleEmailNotifier.class);
    bind(EmailSender.class).to(JavaMailEmailSender.class);
  }
}
