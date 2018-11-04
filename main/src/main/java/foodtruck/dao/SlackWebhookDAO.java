package foodtruck.dao;

import java.util.Optional;

import foodtruck.model.SlackWebhook;

/**
 * @author aviolette
 * @since 10/28/18
 */
public interface SlackWebhookDAO extends DAO<Long, SlackWebhook> {

  Optional<SlackWebhook> findByTeamId(String teamId);
}
