package uk.gov.digital.ho.pttg.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessRepository extends CrudRepository<AccessCodeJpa, Long> {
    Long ACCESS_ID = 1L;

    List<AccessCodeJpa> findByCode(String code);
}
