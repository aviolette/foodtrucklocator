package foodtruck.dao;

import javax.annotation.Nullable;

import foodtruck.model.PetitionSignature;

/**
 * @author aviolette
 * @since 7/29/14
 */
public interface PetitionSignatureDAO extends DAO<Long, PetitionSignature> {
  long findSigned(String petitionId);

  @Nullable PetitionSignature findBySignature(String signature);

  @Nullable PetitionSignature findByEmail(String email);

  @Nullable PetitionSignature findSignedByEmail(String email);
}
