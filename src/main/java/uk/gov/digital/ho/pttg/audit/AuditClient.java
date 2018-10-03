package uk.gov.digital.ho.pttg.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
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
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_ACCESS_CODE_AUDIT_FAILURE;

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
                       @Qualifier(value = "auditRestTemplate") RestTemplate restTemplate,
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
        log.debug("POST data to audit service {}", auditableData);
        dispatchAuditableData(auditableData);
        log.debug("data POSTed to audit service");
    }

    void dispatchAuditableData(AuditableData auditableData) {
        try {
            retryTemplate.execute(context -> {
                if (context.getRetryCount() > 0) {
                    log.info("Retrying audit attempt {} of {}", context.getRetryCount() + 1, maxCallAttempts, value(EVENT, auditableData.getEventType()));
                }
                return restTemplate.exchange(auditEndpoint, POST, toEntity(auditableData), Void.class);
            });
        } catch (Exception e) {
            log.error("Failed to audit {} after retries",auditableData.getEventType(), value(EVENT, HMRC_ACCESS_CODE_AUDIT_FAILURE));
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
        headers.add(RequestData.SESSION_ID_HEADER, requestData.sessionId());
        headers.add(RequestData.CORRELATION_ID_HEADER, requestData.correlationId());
        headers.add(RequestData.USER_ID_HEADER, requestData.userId());

        return headers;
    }
}
