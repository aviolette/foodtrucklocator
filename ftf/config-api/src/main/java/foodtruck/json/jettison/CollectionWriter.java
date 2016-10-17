package foodtruck.json.jettison;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

/**
 * @author aviolette@gmail.com
 * @since 4/19/12
 */
public abstract class CollectionWriter<E, D extends JSONWriter<E>> implements
    MessageBodyWriter<Iterable<E>> {
  private final D writer;
  private final Class<?> entityClass;

  /**
   * Constructs the collection writer
   * @param writer the writer used to output each entity
   * @param entityClass the class of the entities that this collection writer supports
   */
  protected CollectionWriter(D writer, Class<?> entityClass) {
    this.writer = writer;
    this.entityClass = entityClass;
  }

  @Override
  public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations,
      MediaType mediaType) {
    return JSONSerializer.isParameterizedCollectionOf(type, aClass, entityClass);
  }

  @Override
  public long getSize(Iterable<E> es, Class<?> aClass, Type type, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Iterable<E> es, Class<?> aClass, Type type, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> stringObjectMultivaluedMap,
      OutputStream outputStream) throws IOException, WebApplicationException {
    try {
      JSONSerializer.writeJSONCollection(es, writer, outputStream);
    } catch (JSONException e) {
      throw new WebApplicationException(e, 400);
    }
  }

  public JSONArray asJSON(Collection<E> objs) {
    try {
      return JSONSerializer.buildArray(objs, writer);
    } catch (JSONException e) {
      throw new WebApplicationException(e, 400);
    }
  }
}
