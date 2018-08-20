package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_INFERRED")
public class SpringConfigurationTest {

    @Mock
    private RestTemplateBuilder mockRestTemplateBuilder;

    @Mock
    private RestTemplate mockRestTemplate;

    private TimeoutProperties restTemplateProperties;
    private final List<String> allowedSslProtocols = Collections.singletonList("TLSv1.2");

    @Before
    public void setUp() {
        when(mockRestTemplateBuilder.additionalCustomizers(any(ProxyCustomizer.class))).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.additionalMessageConverters(any(HttpMessageConverter.class))).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.setReadTimeout(anyInt())).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.setConnectTimeout(anyInt())).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(mockRestTemplateBuilder);

        when(mockRestTemplateBuilder.build()).thenReturn(mockRestTemplate);

        restTemplateProperties = new TimeoutProperties();
        restTemplateProperties.setAudit(new TimeoutProperties.Audit());
        restTemplateProperties.setHmrc(new TimeoutProperties.Hmrc());
    }

    @Test
    public void shouldUseCustomizerWhenProxyEnabled() {

        SpringConfiguration config = new SpringConfiguration(new ObjectMapper(),
                true, "", "host", 1234, restTemplateProperties, allowedSslProtocols);
        config.auditRestTemplate(mockRestTemplateBuilder, new ObjectMapper());
        verify(mockRestTemplateBuilder).additionalCustomizers(any(ProxyCustomizer.class));
    }

    @Test
    public void shouldNotUseCustomizerByWhenProxyDisabled() {

        SpringConfiguration config = new SpringConfiguration(new ObjectMapper(),
                false, null, null, null, restTemplateProperties, allowedSslProtocols);
        config.auditRestTemplate(mockRestTemplateBuilder, new ObjectMapper());
        verify(mockRestTemplateBuilder, never()).additionalCustomizers(any(ProxyCustomizer.class));
    }

    @Test
    public void shouldSetTimeoutsOnAuditRestTemplate() {
        // given
        final int readTimeout = 1234;
        final int connectTimeout = 4321;

        restTemplateProperties.getAudit().setReadMs(readTimeout);
        restTemplateProperties.getAudit().setConnectMs(connectTimeout);
        SpringConfiguration springConfig = new SpringConfiguration(new ObjectMapper(),
                false, null, null, null, restTemplateProperties, allowedSslProtocols);

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

        restTemplateProperties.getHmrc().setReadMs(readTimeout);
        restTemplateProperties.getHmrc().setConnectMs(connectTimeout);
        SpringConfiguration springConfig = new SpringConfiguration(new ObjectMapper(),
                false, null, null, null, restTemplateProperties, allowedSslProtocols);

        // when
        final ClientHttpRequestFactory clientHttpRequestFactory = springConfig.createClientHttpRequestFactory(springConfig.createHttpClientBuilder());
        RestTemplate restTemplate = springConfig.hmrcRestTemplate(mockRestTemplateBuilder, new ObjectMapper(), clientHttpRequestFactory);

        // then
        verify(mockRestTemplateBuilder).setReadTimeout(readTimeout);
        verify(mockRestTemplateBuilder).setConnectTimeout(connectTimeout);

        assertThat(restTemplate).isEqualTo(mockRestTemplate);
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenNotTlsV1_2() {
        List<String> sslV3Only = Arrays.asList("SSLv3");
        List<String> tlsV1_1Only = Arrays.asList("TLSv1.1");
        List<String> tlsV1_2PlusTlsV1_0 = Arrays.asList("TLSv1.2", "TLSv1.0");
        List<String> noSsl = new ArrayList<>();

        List<List<String>> sslProtocolPermutations = Arrays.asList(sslV3Only, tlsV1_1Only, tlsV1_2PlusTlsV1_0);

        for (List<String> sslProtocols : sslProtocolPermutations) {
            SpringConfiguration springConfiguration = new SpringConfiguration(new ObjectMapper(),false, null, null, null, restTemplateProperties, sslProtocols);
            String expectedErrorMessage = String.format("Invalid TLS/SSL version: %s - only TLSv1.2 permitted.", sslProtocols.get(sslProtocols.size() - 1));
            assertThatThrownBy(springConfiguration::createHttpClientBuilder)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(expectedErrorMessage);
        }

        SpringConfiguration springConfiguration =new SpringConfiguration(new ObjectMapper(),false, null, null, null, restTemplateProperties, noSsl);
        assertThatThrownBy(springConfiguration::createHttpClientBuilder)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid TLS/SSL version: None - only TLSv1.2 permitted.");
    }
}
