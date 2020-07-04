package foodtruck.server.security;

import java.io.IOException;
import java.util.Properties;

public interface PropertyStore {

  Properties findProperties(String name) throws IOException;
}
