package foodtruck.email;

import com.google.inject.AbstractModule;

/**
 * @author aviolette
 * @since 4/29/13
 */
public class EmailModule extends AbstractModule {
  @Override protected void configure() {
    bind(EmailNotifier.class).to(SimpleEmailNotifier.class);
    bind(EmailSender.class).to(JavaMailEmailSender.class);
  }
}
