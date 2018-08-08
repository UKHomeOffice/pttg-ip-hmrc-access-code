package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_INFERRED")
public class SpringConfigurationTest {

    @Mock
    private RestTemplateBuilder mockRestTemplateBuilder;

    @Mock
    private RestTemplate mockRestTemplate;

    private RestTemplateProperties restTemplateProperties;

    @Before
    public void setUp() {
        when(mockRestTemplateBuilder.additionalCustomizers(any(ProxyCustomizer.class))).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.additionalMessageConverters(any(HttpMessageConverter.class))).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.setReadTimeout(anyInt())).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.setConnectTimeout(anyInt())).thenReturn(mockRestTemplateBuilder);

        when(mockRestTemplateBuilder.build()).thenReturn(mockRestTemplate);

        restTemplateProperties = new RestTemplateProperties();
        restTemplateProperties.setAudit(new RestTemplateProperties.Audit());
        restTemplateProperties.setHmrc(new RestTemplateProperties.Hmrc());
        restTemplateProperties.setProxyPort(0);
    }

    @Test
    public void shouldUseCustomizerWhenProxyEnabled() {
        restTemplateProperties.setProxyEnabled(true);
        restTemplateProperties.setHmrcBaseUrl("http://some-fake-hmrc");
        restTemplateProperties.setProxyHost("some-proxy-host");

        SpringConfiguration config = new SpringConfiguration(new ObjectMapper(), restTemplateProperties);
        config.auditRestTemplate(mockRestTemplateBuilder, new ObjectMapper());
        verify(mockRestTemplateBuilder).additionalCustomizers(any(ProxyCustomizer.class));
    }

    @Test
    public void shouldNotUseCustomizerByWhenProxyDisabled() {
        restTemplateProperties.setProxyEnabled(false);

        SpringConfiguration config = new SpringConfiguration(new ObjectMapper(), restTemplateProperties);
        config.auditRestTemplate(mockRestTemplateBuilder, new ObjectMapper());
        verify(mockRestTemplateBuilder, never()).additionalCustomizers(any(ProxyCustomizer.class));
    }

    @Test
    public void shouldSetTimeoutsOnAuditRestTemplate() {
        // given
        final int readTimeout = 1234;
        final int connectTimeout = 4321;

        restTemplateProperties.getAudit().setReadTimeout(readTimeout);
        restTemplateProperties.getAudit().setConnectTimeout(connectTimeout);
        SpringConfiguration springConfig = new SpringConfiguration(new ObjectMapper(), restTemplateProperties);

        // when
        RestTemplate restTemplate = springConfig.auditRestTemplate(mockRestTemplateBuilder, new ObjectMapper());

        // then
        verify(mockRestTemplateBuilder).setReadTimeout(readTimeout);
        verify(mockRestTemplateBuilder).setConnectTimeout(connectTimeout);

        assertThat(restTemplate).isEqualTo(mockRestTemplate);
    }

    @Test
    public void shouldSetTimeoutsOnHmrcRestTemplate() {
        // given
        final int readTimeout = 1234;
        final int connectTimeout = 4321;

        restTemplateProperties.getHmrc().setReadTimeout(readTimeout);
        restTemplateProperties.getHmrc().setConnectTimeout(connectTimeout);
        SpringConfiguration springConfig = new SpringConfiguration(new ObjectMapper(), restTemplateProperties);

        // when
        RestTemplate restTemplate = springConfig.hmrcRestTemplate(mockRestTemplateBuilder, new ObjectMapper());

        // then
        verify(mockRestTemplateBuilder).setReadTimeout(readTimeout);
        verify(mockRestTemplateBuilder).setConnectTimeout(connectTimeout);

        assertThat(restTemplate).isEqualTo(mockRestTemplate);
    }
}
