package foodtruck.alexa;

import foodtruck.util.ServiceException;

/**
 * @author aviolette
 * @since 1/6/18
 */
public interface AmazonConnector {

  /**
   * Finds the address of the configured device
   * @return the response
   * @throws ServiceException if an error occurs in the request
   */
  AddressResponse findAddress() throws ServiceException;
}
