package uk.gov.digital.ho.pttg.application;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableMetrics(proxyTargetClass = true)
@Slf4j
public class DropwizardMetricsConfig extends MetricsConfigurerAdapter {

    @Bean(destroyMethod = "stop")
    @Autowired
    @ConditionalOnProperty(value = "log.metrics", havingValue = "true")
    public Slf4jReporter slf4jReporter(@Qualifier("getMetricRegistry") MetricRegistry registry, @Value("${log.metrics.interval}") int loggingInterval) {
        Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
                .outputTo(log)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(loggingInterval, TimeUnit.MINUTES);
        return reporter;
    }

}