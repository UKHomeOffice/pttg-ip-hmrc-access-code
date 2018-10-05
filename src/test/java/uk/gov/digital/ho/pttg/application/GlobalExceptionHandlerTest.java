package uk.gov.digital.ho.pttg.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.JsonProcessingException;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@RunWith(MockitoJUnitRunner.class)
public class GlobalExceptionHandlerTest {


    private GlobalExceptionHandler handler = new GlobalExceptionHandler();
    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Before
    public void setup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    public void shouldProduceInternalServerErrorForHmrcException() {
        ApplicationExceptions.HmrcAccessCodeServiceRuntimeException mockHmrcException = mock(ApplicationExceptions.HmrcAccessCodeServiceRuntimeException.class);
        when(mockHmrcException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockHmrcException);

        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldLogErrorForForHmrcException() {
        ApplicationExceptions.HmrcAccessCodeServiceRuntimeException mockHmrcException = mock(ApplicationExceptions.HmrcAccessCodeServiceRuntimeException.class);
        when(mockHmrcException.getMessage()).thenReturn("any message");

        handler.handle(mockHmrcException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Responding with 500 after handling any message") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceInternalServerErrorForHmrcACRetreivalException() {
        ApplicationExceptions.HmrcRetrieveAccessCodeException mockHmrcException = mock(ApplicationExceptions.HmrcRetrieveAccessCodeException.class);
        when(mockHmrcException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockHmrcException);

        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldLogErrorForForHmrcACRetreivalException() {
        ApplicationExceptions.HmrcRetrieveAccessCodeException mockHmrcException = mock(ApplicationExceptions.HmrcRetrieveAccessCodeException.class);
        when(mockHmrcException.getMessage()).thenReturn("any message");

        handler.handle(mockHmrcException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Responding with 500 after handling any message") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceHttpUnauthorisedForHmrcUnauthorisedException() {
        ApplicationExceptions.HmrcUnauthorisedException unauthorisedException = mock(ApplicationExceptions.HmrcUnauthorisedException.class);
        when(unauthorisedException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(unauthorisedException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
    }

    @Test
    public void shouldLogErrorForHmrcUnauthorisedException() {
        ApplicationExceptions.HmrcUnauthorisedException unauthorisedException = mock(ApplicationExceptions.HmrcUnauthorisedException.class);
        when(unauthorisedException.getMessage()).thenReturn("any message");

        handler.handle(unauthorisedException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("HmrcUnauthorisedException: any message") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id") &&
                    loggingEvent.getLevel().equals(Level.INFO);
        }));
    }


    @Test
    public void shouldProduceInternalServerErrorForException() {
        Exception mockException = mock(Exception.class);
        when(mockException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldLogErrorForException() {
        Exception mockException = mock(Exception.class);
        when(mockException.getMessage()).thenReturn("any message");

        handler.handle(mockException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Fault Detected:") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceBadRequestErrorForJsonProcessingException() {
        JsonProcessingException mockException = mock(JsonProcessingException.class);
        when(mockException.getMessage()).thenReturn("any message");
        ResponseEntity responseEntity = handler.handle(mockException);
        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldLogErrorForJsonProcessingException() {
        JsonProcessingException mockException = mock(JsonProcessingException.class);
        when(mockException.getMessage()).thenReturn("any message");

        handler.handle(mockException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Responding with 400 after handling any message") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceInternalServerErrorForProxyForbiddenException() {
        ApplicationExceptions.ProxyForbiddenException mockException = mock(ApplicationExceptions.ProxyForbiddenException.class);
        when(mockException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldLogErrorForProxyForbiddenException() {
        ApplicationExceptions.ProxyForbiddenException mockException = mock(ApplicationExceptions.ProxyForbiddenException.class);
        when(mockException.getMessage()).thenReturn("any message");

        handler.handle(mockException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Received 403 Forbidden from a request to HMRC. This was from the proxy and not HMRC.") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceInternalServerErrorForRestClientException() {
        RestClientException mockRestClientException = mock(RestClientException.class);
        when(mockRestClientException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockRestClientException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldLogErrorForRestClientException() {
        RestClientException mockRestClientException = mock(RestClientException.class);
        when(mockRestClientException.getMessage()).thenReturn("any message");

        handler.handle(mockRestClientException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("RestClientException: any message") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }
}