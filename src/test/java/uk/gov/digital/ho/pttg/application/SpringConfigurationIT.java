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
        "resttemplate.audit.read-timeout=1",
        "resttemplate.audit.connect-timeout=2",
        "resttemplate.hmrc.read-timeout=3",
        "resttemplate.hmrc.connect-timeout=4"
})
public class SpringConfigurationIT {

    @Autowired
    private SpringConfiguration springConfiguration;

    @Test
    public void shouldLoadRestTemplateTimeouts() {
        RestTemplateProperties restTemplateProperties = (RestTemplateProperties)ReflectionTestUtils.getField(springConfiguration, "restTemplateProperties");
        if(restTemplateProperties == null) {
            fail("Could not load rest template properties");
        }

        assertThat(restTemplateProperties.getAudit().getReadTimeout()).isEqualTo(1);
        assertThat(restTemplateProperties.getAudit().getConnectTimeout()).isEqualTo(2);
        assertThat(restTemplateProperties.getHmrc().getReadTimeout()).isEqualTo(3);
        assertThat(restTemplateProperties.getHmrc().getConnectTimeout()).isEqualTo(4);
    }

}
