package foodtruck.util;

/**
 * Represents an exception calling an external service
 * @author aviolette@gmail.com
 * @since 10/22/11
 */
public class ServiceException extends RuntimeException {
  public ServiceException() {
    super();
  }

  public ServiceException(Throwable throwable) {
    super(throwable);
  }

  public ServiceException(String message) {
    super(message);
  }
}
