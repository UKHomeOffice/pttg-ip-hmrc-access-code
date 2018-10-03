package uk.gov.digital.ho.pttg.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.RequestData;
import uk.gov.digital.ho.pttg.application.ProxyCustomizer;
import uk.gov.digital.ho.pttg.hmrc.AccessCodeHmrc;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_ACCESS_CODE_REQUEST;

@RunWith(MockitoJUnitRunner.class)
public class AuditClientTest {

    private static TimeZone defaultTimeZone;

    @Captor private ArgumentCaptor<HttpEntity> captorHttpEntity;
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final int RETRY_DELAY = 1;

    @Mock
    private RequestData mockRequestData;
    @Mock
    private RestTemplate mockRestTemplate;
    @Mock
    private Appender<ILoggingEvent> mockAppender;

    private AuditClient client;

    @BeforeClass
    public static void beforeAllTests() {
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterClass
    public static void afterAllTests() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Before
    public void setup(){
        final Clock clock = Clock.fixed(Instant.parse("2017-08-29T08:00:00Z"), ZoneId.of("UTC"));
        client = new AuditClient(clock, mockRestTemplate, mockRequestData, "some endpoint",
                MAX_RETRY_ATTEMPTS, RETRY_DELAY);
    }

    @Test
    public void dispatchAuditableDataShouldRetryOnHttpError() {
        when(mockRestTemplate.exchange(eq("some endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        try {
            client.add(AuditEventType.HMRC_ACCESS_CODE_REQUEST);
        }
        catch (HttpServerErrorException e){
            // Ignore expected exception.
        }

        verify(mockRestTemplate, times(MAX_RETRY_ATTEMPTS)).exchange(eq("some endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    public void shouldUseCollaborators() {
        when(mockRestTemplate.exchange(eq("some endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        client.add(HMRC_ACCESS_CODE_REQUEST);

        verify(mockRestTemplate).exchange(eq("some endpoint"), eq(POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    public void shouldSetHeaders() {
        when(mockRestTemplate.exchange(eq("some endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(mockRequestData.auditBasicAuth()).thenReturn("some basic auth header value");
        when(mockRequestData.sessionId()).thenReturn("some session id");
        when(mockRequestData.correlationId()).thenReturn("some correlation id");
        when(mockRequestData.userId()).thenReturn("some user id");

        client.add(HMRC_ACCESS_CODE_REQUEST);

        verify(mockRestTemplate).exchange(eq("some endpoint"), eq(POST), captorHttpEntity.capture(), eq(Void.class));

        HttpHeaders headers = captorHttpEntity.getValue().getHeaders();

        assertThat(headers.containsKey("Authorization")).isTrue();
        assertThat(requireNonNull(headers.get("Authorization")).size()).isGreaterThan(0);
        assertThat(requireNonNull(headers.get("Authorization")).get(0)).isEqualTo("some basic auth header value");

        assertThat(headers.containsKey("Content-Type")).isTrue();
        assertThat(requireNonNull(headers.get("Content-Type")).size()).isGreaterThan(0);
        assertThat(requireNonNull(headers.get("Content-Type")).get(0)).isEqualTo(APPLICATION_JSON_VALUE);

        assertThat(headers.get(RequestData.SESSION_ID_HEADER).get(0)).isEqualTo("some session id");
        assertThat(headers.get(RequestData.CORRELATION_ID_HEADER).get(0)).isEqualTo("some correlation id");
        assertThat(headers.get(RequestData.USER_ID_HEADER).get(0)).isEqualTo("some user id");
    }

    @Test
    public void shouldSetAuditableData() {
        when(mockRestTemplate.exchange(eq("some endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(mockRequestData.sessionId()).thenReturn("some session id");
        when(mockRequestData.correlationId()).thenReturn("some correlation id");
        when(mockRequestData.userId()).thenReturn("some user id");
        when(mockRequestData.deploymentName()).thenReturn("some deployment name");
        when(mockRequestData.deploymentNamespace()).thenReturn("some deployment namespace");

        client.add(HMRC_ACCESS_CODE_REQUEST);

        verify(mockRestTemplate).exchange(eq("some endpoint"), eq(POST), captorHttpEntity.capture(), eq(Void.class));

        AuditableData auditableData = (AuditableData) captorHttpEntity.getValue().getBody();
        assertThat(auditableData).isNotNull();
        assertThat(auditableData.getEventId()).isNotEmpty();
        assertThat(auditableData.getTimestamp()).isEqualTo(LocalDateTime.parse("2017-08-29T08:00:00"));
        assertThat(auditableData.getSessionId()).isEqualTo("some session id");
        assertThat(auditableData.getCorrelationId()).isEqualTo("some correlation id");
        assertThat(auditableData.getUserId()).isEqualTo("some user id");
        assertThat(auditableData.getDeploymentName()).isEqualTo("some deployment name");
        assertThat(auditableData.getDeploymentNamespace()).isEqualTo("some deployment namespace");
        assertThat(auditableData.getEventType()).isEqualTo(HMRC_ACCESS_CODE_REQUEST);

    }

    @Test
    public void shouldLogFailure() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(AuditClient.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);

        when(mockRestTemplate.exchange(eq("some endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        client.add(HMRC_ACCESS_CODE_REQUEST);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().startsWith("Failed to audit") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

}