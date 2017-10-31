package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableRetry
public class SpringConfiguration extends WebMvcConfigurerAdapter {

    public SpringConfiguration(ObjectMapper objectMapper) {
        initialiseObjectMapper(objectMapper);
    }

    public static void initialiseObjectMapper(ObjectMapper m) {
        m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        m.enable(SerializationFeature.INDENT_OUTPUT);
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    Logger createLogger() {
        return org.slf4j.LoggerFactory.getLogger("uk.gov.digital.ho.pttg");
    }

    @Bean
    RestTemplate createRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    Clock createClock() {
        return Clock.system(ZoneId.of("UTC"));
    }
}