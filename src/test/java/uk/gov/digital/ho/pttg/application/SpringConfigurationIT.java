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
        "timeouts.audit.readSeconds=1",
        "timeouts.audit.connectSeconds=2",
        "timeouts.hmrc.readSeconds=3",
        "timeouts.hmrc.connectSeconds=4"
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

        assertThat(restTemplateProperties.getAudit().getReadSeconds()).isEqualTo(1);
        assertThat(restTemplateProperties.getAudit().getConnectSeconds()).isEqualTo(2);
        assertThat(restTemplateProperties.getHmrc().getReadSeconds()).isEqualTo(3);
        assertThat(restTemplateProperties.getHmrc().getConnectSeconds()).isEqualTo(4);
    }

}
