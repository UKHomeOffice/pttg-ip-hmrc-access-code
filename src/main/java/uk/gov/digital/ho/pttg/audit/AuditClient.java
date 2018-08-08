package uk.gov.digital.ho.pttg.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.RequestData;
import uk.gov.digital.ho.pttg.application.RetryTemplateBuilder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@Component
@Slf4j
public class AuditClient {

    private final Clock clock;
    private final RestTemplate restTemplate;
    private final String auditEndpoint;
    private final RequestData requestData;
    private final RetryTemplate retryTemplate;
    private final int maxCallAttempts;

    public AuditClient(Clock clock,
                       RestTemplate restTemplate,
                       RequestData requestData,
                       @Value("${pttg.audit.endpoint}") String auditEndpoint,
                       @Value("${audit.service.retry.attempts}") int maxCallAttempts,
                       @Value("${audit.service.retry.delay}") int retryDelay) {
        this.clock = clock;
        this.restTemplate = restTemplate;
        this.requestData = requestData;
        this.auditEndpoint = auditEndpoint;
        this.maxCallAttempts = maxCallAttempts;
        this.retryTemplate = new RetryTemplateBuilder(this.maxCallAttempts)
                .withBackOffPeriod(retryDelay)
                .retryHttpServerErrors()
                .build();
    }

  public void add(AuditEventType eventType) {

        AuditableData auditableData = generateAuditableData(eventType);
        log.debug("POST data for correlation id {} to audit service {}", requestData.correlationId(), auditableData);
        dispatchAuditableData(auditableData);
        log.debug("data POSTed for correlation id {} to audit service", requestData.correlationId());
    }

    public void dispatchAuditableData(AuditableData auditableData) {
        try {
            retryTemplate.execute(context -> {
                log.debug("Audit attempt {} of {}", context.getRetryCount() + 1, maxCallAttempts);
                return restTemplate.exchange(auditEndpoint, POST, toEntity(auditableData), Void.class);
            });
        } catch (HttpServerErrorException e) {
            log.error("Failed to audit after retries due to {}", e.getMessage(), value(EVENT, HMRC_ACCESS_CODE_AUDIT_FAILURE));
            //throw e;
        }
    }

    private AuditableData generateAuditableData(AuditEventType eventType) {
        return new AuditableData(UUID.randomUUID().toString(),
                                    LocalDateTime.now(clock),
                                    requestData.sessionId(),
                                    requestData.correlationId(),
                                    requestData.userId(),
                                    requestData.deploymentName(),
                                    requestData.deploymentNamespace(),
                                    eventType,
                                    "{}");
    }

    private HttpEntity<AuditableData> toEntity(AuditableData auditableData) {
        return new HttpEntity<>(auditableData, generateRestHeaders());
    }

    private HttpHeaders generateRestHeaders() {

        HttpHeaders headers = new HttpHeaders();

        headers.add(AUTHORIZATION, requestData.auditBasicAuth());
        headers.setContentType(APPLICATION_JSON);

        return headers;
    }
}
