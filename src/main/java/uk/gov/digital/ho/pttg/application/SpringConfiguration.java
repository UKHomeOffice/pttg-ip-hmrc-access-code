package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class SpringConfiguration extends WebMvcConfigurerAdapter {

    private final boolean useProxy;
    private final String hmrcBaseUrl;
    private final String proxyHost;
    private final Integer proxyPort;

    public SpringConfiguration(ObjectMapper objectMapper,
                               @Value("${proxy.enabled:false}") boolean useProxy,
                               @Value("${hmrc.endpoint:}") String hmrcBaseUrl,
                               @Value("${proxy.host:}") String proxyHost,
                               @Value("${proxy.port}") Integer proxyPort) {

        this.useProxy =useProxy;
        this.hmrcBaseUrl = hmrcBaseUrl;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        initialiseObjectMapper(objectMapper);
    }

    private static ObjectMapper initialiseObjectMapper(final ObjectMapper m) {
        m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        m.enable(SerializationFeature.INDENT_OUTPUT);
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return m;
    }

    @Bean
    public RestTemplate createRestTemplate(RestTemplateBuilder builder, ObjectMapper mapper) {

        if (useProxy) {
            builder = builder.additionalCustomizers(createProxyCustomiser());
        }

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);


        return builder.additionalMessageConverters(converter).build();
    }

    ProxyCustomizer createProxyCustomiser() {
        return new ProxyCustomizer(hmrcBaseUrl, proxyHost, proxyPort.intValue());
    }



    @Bean
    Clock createClock() {
        return Clock.system(ZoneId.of("UTC"));
    }


}