package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
@SpringBootConfiguration
@RunWith(MockitoJUnitRunner.class)
public class SpringConfigurationTest {

    @Mock
    private RestTemplateBuilder mockRestTemplateBuilder;

    @Mock
    private RestTemplate mockRestTemplate;

    @Before
    public void setUp() {
        when(mockRestTemplateBuilder.additionalCustomizers(any(ProxyCustomizer.class))).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.additionalMessageConverters(any(HttpMessageConverter.class))).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.setReadTimeout(anyInt())).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.setConnectTimeout(anyInt())).thenReturn(mockRestTemplateBuilder);

        when(mockRestTemplateBuilder.build()).thenReturn(mockRestTemplate);
    }

    @Test
    public void shouldUseCustomizerWhenProxyEnabled() {

        SpringConfiguration config = new SpringConfiguration(new ObjectMapper(),
                true, "", "host", 1234, 0, 0);
        config.createRestTemplate(mockRestTemplateBuilder, new ObjectMapper());
        verify(mockRestTemplateBuilder).additionalCustomizers(any(ProxyCustomizer.class));
    }

    @Test
    public void shouldNotUseCustomizerByWhenProxyDisabled() {

        SpringConfiguration config = new SpringConfiguration(new ObjectMapper(),
                false, null, null, null, 0, 0);
        config.createRestTemplate(mockRestTemplateBuilder, new ObjectMapper());
        verify(mockRestTemplateBuilder, never()).additionalCustomizers(any(ProxyCustomizer.class));
    }

    @Test
    public void shouldSetTimeoutsOnRestTemplate() {
        // given
        int readTimeout = 1234;
        int connectTimeout = 4321;
        SpringConfiguration springConfig = new SpringConfiguration(new ObjectMapper(), false, null, null, null, readTimeout, connectTimeout);

        // when
        RestTemplate restTemplate = springConfig.createRestTemplate(mockRestTemplateBuilder, new ObjectMapper());

        // then
        verify(mockRestTemplateBuilder).setReadTimeout(readTimeout);
        verify(mockRestTemplateBuilder).setConnectTimeout(connectTimeout);

        assertThat(restTemplate).isEqualTo(mockRestTemplate);
    }
}
