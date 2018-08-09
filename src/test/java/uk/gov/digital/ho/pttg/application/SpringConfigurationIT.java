package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TimeoutProperties.class, SpringConfigurationIT.TestConfig.class})
@TestPropertySource(properties = {
        "timeouts.audit.read-ms=1000",
        "timeouts.audit.connect-ms=2000",
        "timeouts.hmrc.read-ms=3000",
        "timeouts.hmrc.connect-ms=4000"
})
public class SpringConfigurationIT {

    @TestConfiguration
    @EnableConfigurationProperties
    public static class TestConfig {}

    @Autowired
    private TimeoutProperties timeoutProperties;

    @Test
    public void shouldLoadRestTemplateTimeouts() {
        assertThat(timeoutProperties.getAudit().getReadMs()).isEqualTo(1000);
        assertThat(timeoutProperties.getAudit().getConnectMs()).isEqualTo(2000);
        assertThat(timeoutProperties.getHmrc().getReadMs()).isEqualTo(3000);
        assertThat(timeoutProperties.getHmrc().getConnectMs()).isEqualTo(4000);
    }

}
