package foodtruck.server.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import com.google.appengine.api.utils.SystemProperty;
import com.google.inject.Inject;

import foodtruck.crypto.SymmetricCrypto;
import foodtruck.storage.StorageService;

public class EncryptedPropertyStore implements PropertyStore {

  private final StorageService storageService;
  private final SymmetricCrypto crypto;

  @Inject
  public EncryptedPropertyStore(StorageService storageService, SymmetricCrypto crypto) {
    this.storageService = storageService;
    this.crypto = crypto;
  }

  @Override
  public Properties findProperties(String name) throws IOException {
    Properties properties = new Properties();
    if (SystemProperty.environment.value() != SystemProperty.Environment.Value.Development) {
      ByteArrayOutputStream bas = new ByteArrayOutputStream();
      storageService.readStream("cftf_secrets", name + ".properties.encrypted", bas);
      byte[] propertyArray = crypto.decrypt(bas.toByteArray());
      try (StringReader reader = new StringReader(new String(propertyArray, StandardCharsets.UTF_8))) {
        properties.load(reader);
      }
    } else {
      properties.load(EncryptedPropertyStore.class.getClassLoader().getResourceAsStream(name + ".properties"));
    }
    return properties;
  }
}
