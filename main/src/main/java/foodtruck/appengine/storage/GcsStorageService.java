package foodtruck.appengine.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;

import foodtruck.storage.StorageService;

/**
 * @author aviolette
 * @since 12/2/16
 */
public class GcsStorageService implements StorageService {

  private static final Logger log = Logger.getLogger(GcsStorageService.class.getName());

  private final GcsService cloudStorage;

  @Inject
  public GcsStorageService(GcsService cloudStorage) {
    this.cloudStorage = cloudStorage;
  }

  @Override
  public String copyUrl(String fromUrl, String bucket, String fileName) throws IOException {
    URL iconUrl = new URL(fromUrl);
    try (InputStream in = iconUrl.openStream()) {
      return writeImage(in, bucket, fileName);
    }
  }

  @Override
  public String writeImage(InputStream inputStream, String bucket, String fileName) throws IOException {
    return writeStream(inputStream, bucket, fileName, fileName.matches("png") ? "image/png" : "image/jpeg");
  }

  @Override
  public String writeBuffer(byte[] buffer, String bucket, String fileName,
      String mimeType) throws IOException {
    return writeStream(new ByteArrayInputStream(buffer), bucket, fileName, mimeType);
  }

  @Override
  public void readStream(String bucket, String file, OutputStream outputStream) throws IOException {
    GcsFilename gcsFilename = new GcsFilename(bucket, file);
    GcsInputChannel channel = cloudStorage.openReadChannel(gcsFilename, 0);
    try (InputStream is = Channels.newInputStream(channel)) {
      long copied = ByteStreams.copy(is, outputStream);
      log.log(Level.INFO, "Downloaded {0} bytes from {1}/{2}", new Object[] {copied, bucket, file});
    }
  }

  private String writeStream(InputStream stream, String bucket, String fileName, String mimeType) throws IOException {
    GcsFilename gcsFilename = new GcsFilename(bucket, fileName);
    GcsOutputChannel channel = cloudStorage.createOrReplace(gcsFilename,
        new GcsFileOptions.Builder().cacheControl("public, max-age=2591000")
            .mimeType(mimeType)
            .build());
    try (OutputStream out = Channels.newOutputStream(channel)) {
      long copied = ByteStreams.copy(stream, out);
      log.log(Level.INFO, "Uploaded {0} bytes to {1}/{2}", new Object[] {copied, bucket, fileName});
    }
    return "http://storage.googleapis.com/" + bucket + "/" + fileName;
  }
}
