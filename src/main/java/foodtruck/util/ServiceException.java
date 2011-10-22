package foodtruck.util;

/**
 * Represents an exception calling an external service
 * @author aviolette@gmail.com
 * @since 10/22/11
 */
public class ServiceException extends Exception {
  public ServiceException() {
    super();
  }

  public ServiceException(String message) {
    super(message);
  }

  public ServiceException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public ServiceException(Throwable throwable) {
    super(throwable);
  }
}
