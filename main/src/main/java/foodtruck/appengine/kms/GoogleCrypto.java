package foodtruck.appengine.kms;

import java.io.IOException;

import com.google.cloud.kms.v1.DecryptResponse;
import com.google.cloud.kms.v1.EncryptResponse;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.inject.Inject;
import com.google.protobuf.ByteString;

import foodtruck.annotations.DefaultCryptoKey;
import foodtruck.crypto.SymmetricCrypto;

public class GoogleCrypto implements SymmetricCrypto {

  private final String cryptoKeyName;

  @Inject
  public GoogleCrypto(@DefaultCryptoKey String cryptoKeyName) {
    this.cryptoKeyName = cryptoKeyName;
  }

  // to encrypt from command line https://cloud.google.com/kms/docs/encrypt-decrypt
  @Override
  public byte[] encrypt(byte[] plaintext) throws IOException {
    try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
      EncryptResponse response = client.encrypt(cryptoKeyName, ByteString.copyFrom(plaintext));
      return response.getCiphertext()
          .toByteArray();
    }
  }

  public byte[] decrypt(byte[] text) throws IOException {
    try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
      DecryptResponse response = client.decrypt(cryptoKeyName, ByteString.copyFrom(text));
      return response.getPlaintext()
          .toByteArray();
    }
  }
}
