package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.digital.ho.pttg.api.RequestData;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.ZoneId;
import java.util.List;

@Configuration
@EnableRetry
public class SpringConfiguration implements WebMvcConfigurer {

    private final boolean useProxy;
    private final String hmrcBaseUrl;
    private final String proxyHost;
    private final Integer proxyPort;
    private final TimeoutProperties timeoutProperties;
    private final String[] supportedSslProtocols;

    SpringConfiguration(ObjectMapper objectMapper,
                        @Value("${proxy.enabled:false}") boolean useProxy,
                        @Value("${hmrc.endpoint:}") String hmrcBaseUrl,
                        @Value("${proxy.host:}") String proxyHost,
                        @Value("${proxy.port}") Integer proxyPort,
                        TimeoutProperties timeoutProperties,
                        @Value("#{'${hmrc.ssl.supportedProtocols}'.split(',')}") List<String> supportedSslProtocols
    ) {
        this.useProxy = useProxy;
        this.hmrcBaseUrl = hmrcBaseUrl;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.timeoutProperties = timeoutProperties;
        this.supportedSslProtocols = supportedSslProtocols.toArray(new String[]{});
        
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
    RestTemplate auditRestTemplate(RestTemplateBuilder restTemplateBuilder, ObjectMapper mapper) {
        RestTemplateBuilder builder = initaliseRestTemplateBuilder(restTemplateBuilder, mapper);

        return builder
                .setReadTimeout(timeoutProperties.getAudit().getReadMs())
                .setConnectTimeout(timeoutProperties.getAudit().getConnectMs())
                .build();
    }

    @Bean
    RestTemplate hmrcRestTemplate(RestTemplateBuilder restTemplateBuilder, ObjectMapper mapper, ClientHttpRequestFactory clientHttpRequestFactory) {
        RestTemplateBuilder builder = initaliseRestTemplateBuilder(restTemplateBuilder, mapper);

        return builder
                .setReadTimeout(timeoutProperties.getHmrc().getReadMs())
                .setConnectTimeout(timeoutProperties.getHmrc().getConnectMs())
                .requestFactory(() -> clientHttpRequestFactory)
                .build();
    }

    @Bean
    public ClientHttpRequestFactory createClientHttpRequestFactory(HttpClientBuilder builder) {

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(builder.build());

        return factory;
    }

    @Bean
    public HttpClientBuilder createHttpClientBuilder() {
        final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(SSLContexts.createDefault(), supportedSslProtocols, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        return HttpClientBuilder.create().setSSLSocketFactory(sslSocketFactory);
    }


    private RestTemplateBuilder initaliseRestTemplateBuilder(RestTemplateBuilder restTemplateBuilder, ObjectMapper mapper) {
        RestTemplateBuilder builder = restTemplateBuilder;

        if (useProxy) {
            builder = builder.additionalCustomizers(createProxyCustomizer());
        }

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);
        builder = builder.additionalMessageConverters(converter);

        return builder;
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