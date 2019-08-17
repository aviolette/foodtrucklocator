package foodtruck.appengine.kms;

import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import foodtruck.annotations.DefaultCryptoKey;
import foodtruck.crypto.SymmetricCrypto;

public class GoogleCryptoModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(SymmetricCrypto.class).to(GoogleCrypto.class);
  }

  @Provides
  @DefaultCryptoKey
  public String providesDefaultCryptoKey() {
    return CryptoKeyName.format("chicagofoodtrucklocator", "us-central1", "foodtruckring" , "default");
  }
}
