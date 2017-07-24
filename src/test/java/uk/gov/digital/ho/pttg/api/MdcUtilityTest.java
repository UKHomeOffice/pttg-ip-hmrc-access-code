package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MdcUtilityTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MdcUtility mdcUtility;

    @Before
    public void setup() {
        mdcUtility = new MdcUtility();
        assertThat(thrown).isNotNull(); // ridiculous way of getting round findbugs squawking
    }

    @Test
    public void shouldGenerateDefaultUserId() {
        assertThat(mdcUtility.generateDefaultUserId()).isEqualTo("Anonymous");
    }

    @Test
    public void shouldGenerateUuidForCorrelationId() {
        String correlationId = mdcUtility.generateCorrelationId();
        UUID uuid = UUID.fromString(correlationId);
        assertThat(uuid).isNotNull(); // Only interested in the above 'fromString' not throwing an exception - this is to stop findbugs whinging
    }

}