package foodtruck.json.jackson;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * @author aviolette
 * @since 12/5/16
 */
public class JacksonModule extends AbstractModule {
  @Override
  protected void configure() {
  }

  @Provides
  @Singleton
  ObjectMapper provideObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    objectMapper.configure(MapperFeature.AUTO_DETECT_GETTERS, false);
    objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.registerModule(new GuavaModule());
    objectMapper.registerModule(new JodaModule());

    objectMapper.setVisibilityChecker(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
        // Auto-detect fields regardless of visibility - default is public only
        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
        // Do not use getter methods when serializing json, if you need to include a getter as json
        // use the JsonGetter annotation
        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withSetterVisibility(JsonAutoDetect.Visibility.NONE));

    objectMapper.registerModule(new SimpleModule()
        .addSerializer(DateTime.class, new JacksonDateTimeSerializer())
        .addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer())
        .addDeserializer(DateTime.class, new JacksonDateTimeDeserializer()));

    return objectMapper;
  }
}
