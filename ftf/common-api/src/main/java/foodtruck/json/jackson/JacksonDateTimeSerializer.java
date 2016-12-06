package foodtruck.json.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.joda.time.DateTime;

/**
 * @author aviolette
 * @since 12/5/16
 */
public class JacksonDateTimeSerializer extends JsonSerializer<DateTime> {

  @Override
  public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
    jgen.writeString(JacksonDateTimeDeserializer.ISO8601_FORMATTER.print(value));
  }

}
