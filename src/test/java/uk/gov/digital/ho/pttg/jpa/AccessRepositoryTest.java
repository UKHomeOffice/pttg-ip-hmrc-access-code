package uk.gov.digital.ho.pttg.jpa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.jpa.AccessRepository.ACCESS_ID;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AccessRepositoryTest {

    private static final String ACCESS_CODE = "newcode";

    @Autowired
    private AccessRepository repository;

    @Test
    public void shouldRetrieveAccessCode() {
        assertThat(repository.findById(ACCESS_ID).get()).hasNoNullFieldsOrProperties();
    }

    @Test
    public void shouldOnlyEverBeOneRow() {
        repository.save(new AccessCodeJpa(LocalDateTime.now().plusMinutes(3), LocalDateTime.now().plusMinutes(1), ACCESS_CODE));
        assertThat(repository.findAll()).hasSize(1);
        assertThat(repository.findById(ACCESS_ID).get()).hasFieldOrPropertyWithValue("code",ACCESS_CODE);
    }
}