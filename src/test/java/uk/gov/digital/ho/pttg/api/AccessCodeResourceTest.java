package uk.gov.digital.ho.pttg.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessCodeResourceTest {

    @Mock
    private AccessCode stubAccessCode;

    @Mock
    private AccessCodeService mockAccessCodeService;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    private AccessCodeResource resource;

    @Before
    public void setup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(AccessCodeResource.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
        resource = new AccessCodeResource(mockAccessCodeService);
    }

    @Test
    public void shouldUseCollaborators_getAccessCode() throws IOException {

        when(mockAccessCodeService.getAccessCode()).thenReturn(stubAccessCode);

        resource.getAccessCode();

        verify(mockAccessCodeService).getAccessCode();
    }

    @Test
    public void shouldReturnAccessCodeResponse() throws IOException {

        when(mockAccessCodeService.getAccessCode()).thenReturn(stubAccessCode);

        ResponseEntity response = resource.getAccessCode();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((AccessCode)response.getBody())).isEqualTo(stubAccessCode);
    }

    @Test
    public void shouldUseCollaborators_refresh() throws IOException {

        resource.refresh();

        verify(mockAccessCodeService).refreshAccessCode();
    }


    @Test
    public void shouldLogWhenGetAccessCodeRequestReceived() {
        resource.getAccessCode();
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Calling AccessCodeService to produce new access code") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogGetAccessCodeResponseSuccess() {
        resource.getAccessCode();
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("AccessCodeService returned access code") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogWhenRefreshRequestReceived() {
        resource.refresh();
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Calling AccessCodeService to refresh access code") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogRefreshResponseSuccess() {
        resource.refresh();
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("AccessCodeService refreshed access code") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

}