package uk.gov.digital.ho.pttg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@SpringBootApplication
@Slf4j
public class ServiceRunner {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRunner.class, args);
        log.info("HMRC Access Code Service Started", value(EVENT, HMRC_ACCESS_CODE_SERVICE_STARTED));
    }

}
