package uk.gov.digital.ho.pttg.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessRepository extends CrudRepository<AccessCodeJpa, Long> {
    Long ACCESS_ID = 1L;
}
