package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.digital.ho.pttg.api.RequestData;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableRetry
public class SpringConfiguration implements WebMvcConfigurer {

    private final boolean useProxy;
    private final String hmrcBaseUrl;
    private final String proxyHost;
    private final Integer proxyPort;

    private final int restTemplateReadTimeoutInMillis;
    private final int restTemplateConnectTimeoutInMillis;

    SpringConfiguration(ObjectMapper objectMapper,
                        @Value("${proxy.enabled:false}") boolean useProxy,
                        @Value("${hmrc.endpoint:}") String hmrcBaseUrl,
                        @Value("${proxy.host:}") String proxyHost,
                        @Value("${proxy.port}") Integer proxyPort,
                        @Value("${resttemplate.timeout.read:30000}") int restTemplateReadTimeoutInMillis,
                        @Value("${resttemplate.timeout.connect:30000}") int restTemplateConnectTimeoutInMillis) {

        this.useProxy = useProxy;
        this.hmrcBaseUrl = hmrcBaseUrl;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.restTemplateReadTimeoutInMillis = restTemplateReadTimeoutInMillis;
        this.restTemplateConnectTimeoutInMillis = restTemplateConnectTimeoutInMillis;
        initialiseObjectMapper(objectMapper);
    }

    public static ObjectMapper initialiseObjectMapper(final ObjectMapper m) {
        m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        m.enable(SerializationFeature.INDENT_OUTPUT);
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return m;
    }

    @Bean
    RestTemplate createRestTemplate(RestTemplateBuilder restTemplateBuilder, ObjectMapper mapper) {
        if (useProxy) {
            restTemplateBuilder = restTemplateBuilder.additionalCustomizers(createProxyCustomizer());
        }

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);

        return restTemplateBuilder
                .additionalMessageConverters(converter)
                .setReadTimeout(restTemplateReadTimeoutInMillis)
                .setConnectTimeout(restTemplateConnectTimeoutInMillis)
                .build();
    }

    private ProxyCustomizer createProxyCustomizer() {
        return new ProxyCustomizer(hmrcBaseUrl, proxyHost, proxyPort);
    }

    @Bean
    Clock createClock() {
        return Clock.system(ZoneId.of("UTC"));
    }

    @Bean
    public RequestData createRequestData() {
        return new RequestData();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(createRequestData());
    }

}