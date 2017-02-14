package foodtruck.appengine.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;

import com.google.api.client.util.ByteStreams;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.inject.Inject;

import foodtruck.storage.StorageService;

/**
 * @author aviolette
 * @since 12/2/16
 */
public class GcsStorageService implements StorageService {

  private final GcsService cloudStorage;

  @Inject
  public GcsStorageService(GcsService cloudStorage) {
    this.cloudStorage = cloudStorage;
  }

  @Override
  public String copyUrl(String fromUrl, String bucket, String fileName) throws IOException {
    URL iconUrl = new URL(fromUrl);
    try (InputStream in = iconUrl.openStream()) {
      return syncStream(in, bucket, fileName);
    }
  }

  @Override
  public String syncStream(InputStream inputStream, String bucket, String fileName) throws IOException {
    GcsFilename gcsFilename = new GcsFilename(bucket, fileName);
    GcsOutputChannel channel = cloudStorage.createOrReplace(gcsFilename,
        new GcsFileOptions.Builder().cacheControl("public, max-age=2591000")
            .mimeType(fileName.matches("png") ? "image/png" : "image/jpeg")
            .build());
    try (OutputStream out = Channels.newOutputStream(channel)) {
      ByteStreams.copy(inputStream, out);
    }
    return "http://storage.googleapis.com/" + bucket + "/" + fileName;
  }
}
