package foodtruck.dao.appengine;

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.inject.Inject;

import org.joda.time.DateTimeZone;

import foodtruck.dao.PetitionSignatureDAO;
import foodtruck.model.PetitionSignature;
import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;
import static foodtruck.dao.appengine.Attributes.getDateTime;
import static foodtruck.dao.appengine.Attributes.getStringProperty;
import static foodtruck.dao.appengine.Attributes.setDateProperty;

/**
 * @author aviolette
 * @since 7/29/14
 */
public class PetitionSignatureDAOAppEngine extends AppEngineDAO<Long, PetitionSignature> implements
    PetitionSignatureDAO {

  private static final String LAST_NAME_FIELD = "last_name";
  private static final String FIRST_NAME_FIELD = "first_name";
  private static final String IN_WARD_FIELD = "in_ward";
  private static final String EMAIL_FIELD = "email";
  private static final String CREATED_FIELD = "created";
  private static final String SIGNED_FIELD = "signed";
  private static final String PETITION_FIELD = "petition_id";
  private static final String SIGNATURE_FIELD = "signature_field";
  private static final String ZIPCODE_FIELD = "zipcode";
  private static String KIND = "petition_signature";
  private final DateTimeZone zone;

  @Inject
  public PetitionSignatureDAOAppEngine(DatastoreServiceProvider provider, DateTimeZone zone) {
    super(KIND, provider);
    this.zone = zone;
  }

  @Override protected Entity toEntity(PetitionSignature obj, Entity entity) {
    entity.setProperty(LAST_NAME_FIELD, obj.getLastName());
    entity.setProperty(FIRST_NAME_FIELD, obj.getFirstName());
    entity.setProperty(IN_WARD_FIELD, obj.isInWard());
    entity.setProperty(EMAIL_FIELD,obj.getEmail());
    setDateProperty(CREATED_FIELD, entity, obj.getCreated());
    setDateProperty(SIGNED_FIELD, entity, obj.getSigned());
    entity.setProperty(PETITION_FIELD, obj.getPetitionId());
    entity.setProperty(SIGNATURE_FIELD, obj.getSignature());
    entity.setProperty(ZIPCODE_FIELD, obj.getZipcode());
    return entity;
  }

  @Override protected PetitionSignature fromEntity(Entity entity) {
    return PetitionSignature.builder()
        .key(entity.getKey().getId())
        .lastName(getStringProperty(entity, LAST_NAME_FIELD))
        .firstName(getStringProperty(entity, FIRST_NAME_FIELD))
        .inWard(getBooleanProperty(entity, IN_WARD_FIELD, false))
        .email(getStringProperty(entity, EMAIL_FIELD))
        .created(getDateTime(entity, CREATED_FIELD, zone))
        .signed(getDateTime(entity, SIGNED_FIELD, zone))
        .petitionId(getStringProperty(entity, PETITION_FIELD))
        .signature(getStringProperty(entity, SIGNATURE_FIELD))
        .zipcode(getStringProperty(entity, ZIPCODE_FIELD))
        .build();
  }

  @Override public long findSigned(String petitionId) {
    DatastoreService dataStore = provider.get();
    Query q = new Query(KIND);
    Query.Filter truckIdFilter = new Query.FilterPredicate(PETITION_FIELD, Query.FilterOperator.EQUAL, petitionId);
    Query.Filter twitterHandleFilter = new Query.FilterPredicate(SIGNED_FIELD, Query.FilterOperator.NOT_EQUAL, null);
    q.setFilter(Query.CompositeFilterOperator.and(truckIdFilter, twitterHandleFilter));
    return dataStore.prepare(q).countEntities(withDefaults());
  }

  @Override public @Nullable PetitionSignature findBySignature(String signature) {
    return findSingleItemByAttribute(SIGNATURE_FIELD, signature);
  }

  @Nullable @Override public PetitionSignature findByEmail(String email) {
    return findSingleItemByAttribute(EMAIL_FIELD, email);
  }

  @Nullable @Override public PetitionSignature findSignedByEmail(String email) {
    Query.CompositeFilter filter =
        Query.CompositeFilterOperator.and(
            new Query.FilterPredicate(EMAIL_FIELD, Query.FilterOperator.EQUAL, email),
            new Query.FilterPredicate(SIGNED_FIELD, Query.FilterOperator.NOT_EQUAL, null));
    return findSingleItemByFilter(filter);
  }
}
