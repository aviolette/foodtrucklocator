package foodtruck.dao;

import java.util.List;

import foodtruck.model.AlexaExchange;

/**
 * @author aviolette
 * @since 9/16/16
 */
public interface AlexaExchangeDAO extends DAO<Long, AlexaExchange> {
  List<AlexaExchange> findMostRecentOfIntent(String intentName);
}
