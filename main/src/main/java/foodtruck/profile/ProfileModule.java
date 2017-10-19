package foodtruck.profile;

import com.google.inject.AbstractModule;

/**
 * @author aviolette
 * @since 12/2/16
 */
public class ProfileModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ProfileSyncService.class).to(ProfileSyncServiceImpl.class);
  }
}
