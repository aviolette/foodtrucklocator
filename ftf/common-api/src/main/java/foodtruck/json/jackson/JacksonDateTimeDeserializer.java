package foodtruck.json.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author aviolette
 * @since 12/5/16
 */
public class JacksonDateTimeDeserializer extends JsonDeserializer<DateTime> {
  static final DateTimeFormatter LEGACY_FORMATTER = DateTimeFormat.forPattern("yyyyMMdd-HHmm")
      .withZone(DateTimeZone.UTC);
  static final DateTimeFormatter ISO8601_FORMATTER = ISODateTimeFormat.dateTime()
      .withZone(DateTimeZone.UTC);

  @Override
  public DateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    JsonNode node = jp.getCodec()
        .readTree(jp);
    try {
      return ISO8601_FORMATTER.parseDateTime(node.asText());
    } catch (IllegalArgumentException iae) {
      return LEGACY_FORMATTER.parseDateTime(node.asText());
    }
  }
}