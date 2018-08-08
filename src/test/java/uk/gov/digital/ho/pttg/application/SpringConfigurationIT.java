package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {
        "timeouts.audit.read-ms=1000",
        "timeouts.audit.connect-ms=2000",
        "timeouts.hmrc.read-ms=3000",
        "timeouts.hmrc.connect-ms=4000"
})
public class SpringConfigurationIT {

    @Autowired
    private SpringConfiguration springConfiguration;

    @Test
    public void shouldLoadRestTemplateTimeouts() {
        TimeoutProperties restTemplateProperties = (TimeoutProperties)ReflectionTestUtils.getField(springConfiguration, "timeoutProperties");
        if(restTemplateProperties == null) {
            fail("Could not load timeout properties");
        }

        assertThat(restTemplateProperties.getAudit().getReadMs()).isEqualTo(1000);
        assertThat(restTemplateProperties.getAudit().getConnectMs()).isEqualTo(2000);
        assertThat(restTemplateProperties.getHmrc().getReadMs()).isEqualTo(3000);
        assertThat(restTemplateProperties.getHmrc().getConnectMs()).isEqualTo(4000);
    }

}
