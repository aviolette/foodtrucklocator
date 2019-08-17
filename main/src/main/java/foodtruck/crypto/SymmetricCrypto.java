package foodtruck.crypto;

import java.io.IOException;

public interface SymmetricCrypto {
  byte[] encrypt(byte[] text) throws IOException;

  byte[] decrypt(byte[] encryptedText) throws IOException;
}
