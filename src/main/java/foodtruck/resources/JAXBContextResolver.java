package foodtruck.resources;

import java.util.Set;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.sun.jersey.spi.scanning.AnnotationScannerListener;

/**
 * @author aviolette@gmail.com
 * @since 9/5/11
 */
@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext> {
  private JAXBContext context;
  private final Class<?>[] types;

  @SuppressWarnings("unchecked")
  public JAXBContextResolver() throws JAXBException {
    final String modelPackages[] = {"foodtruck.model"};
    PackageNamesScanner pns = new PackageNamesScanner(modelPackages);
    AnnotationScannerListener asl =
        new AnnotationScannerListener(XmlRootElement.class, XmlType.class);
    pns.scan(asl);
    Set<Class<?>> jaxbClasses = asl.getAnnotatedClasses();
    types = jaxbClasses.toArray(new Class[jaxbClasses.size()]);
    final JSONConfiguration configuration = JSONConfiguration.natural().build();
    this.context = new JSONJAXBContext(configuration, types);
  }

  public JAXBContext getContext(Class<?> objectType) {
    for (Class<?> type : types) {
      if (type == objectType) {
        return context;
      }
    }
    return null;
  }
}
