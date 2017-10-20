package uk.gov.digital.ho.pttg.application;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableMetrics(proxyTargetClass = true)
@Slf4j
public class DropwizardMetricsConfig extends MetricsConfigurerAdapter {

    @Qualifier("getMetricRegistry")
    @Autowired
    private MetricRegistry registry;

    /*@Bean(destroyMethod = "stop")
    public GraphiteReporter graphiteReporter() {
        GraphiteSender sender = new Graphite("localhost", 2003);
        GraphiteReporter reporter = GraphiteReporter.forRegistry(registry).prefixedWith("access-code").convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build(sender);
        reporter.start(10, TimeUnit.SECONDS);
        return reporter;
    }*/

    @Bean(destroyMethod = "stop")
    public Slf4jReporter slf4jReporter() {
        Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
                .outputTo(log)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(5, TimeUnit.MINUTES);
        return reporter;
    }


 
}