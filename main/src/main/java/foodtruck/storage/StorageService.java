package foodtruck.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author aviolette
 * @since 12/2/16
 */
public interface StorageService {
  /**
   * Copies a URL from a URL to
   * @param fromUrl a URL somewhere out there
   * @param toBucket the bucket on the remote system where the file will be created or updated
   * @param destinationFileName the new file name of the copied URL
   * @throws IOException if an error occurs in the process
   * @return the URL of the new resource
   */
  String copyUrl(String fromUrl, String toBucket, String destinationFileName) throws IOException;

  String writeImage(InputStream stream, String toBucket, String destinationFileName) throws IOException;

  String writeBuffer(byte[] buffer, String toBucket, String destinationFileName, String mimeType) throws IOException;

  void readStream(String bucket, String file, OutputStream outputStream) throws IOException;
}
