package uk.gov.digital.ho.pttg.hmrc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_GET_ACCESS_CODE_SUCCESS;

@RunWith(MockitoJUnitRunner.class)
public class HmrcClientTest {

    @Mock
    private RestTemplate mockRestTemplate;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor private ArgumentCaptor<String> captorUrl;
    @Captor private ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captorRequest;

    private HmrcClient hmrcClient;

    @Before
    public void setup() {
        hmrcClient = new HmrcClient(mockRestTemplate, "some client id", "some url");
        Logger rootLogger = (Logger) LoggerFactory.getLogger(HmrcClient.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    public void shouldUseCollaborators() {

        ResponseEntity<AccessCodeHmrc> responseEntity = new ResponseEntity<>(new AccessCodeHmrc("some code", 0, "some token"), HttpStatus.OK);

        when(mockRestTemplate.postForEntity(
                anyString(),
                ArgumentMatchers.<Class<HttpEntity<MultiValueMap<String, String>>>>any(),
                ArgumentMatchers.<Class<AccessCodeHmrc>>any()))
                .thenReturn(responseEntity);

        AccessCodeHmrc accessCode = hmrcClient.getAccessCodeFromHmrc("some totp code");

        assertThat(accessCode).isEqualTo(new AccessCodeHmrc("some code", 0, "some token"));

        verify(mockRestTemplate).postForEntity(captorUrl.capture(), captorRequest.capture(), any());

        assertThat(captorUrl.getValue()).isEqualTo("some url/oauth/token");

        HttpEntity<MultiValueMap<String, String>> request = captorRequest.getValue();
        assertThat(request.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE)).isTrue();
        assertThat(request.getHeaders().getContentType()).isEqualTo(APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = request.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("grant_type").size()).isEqualTo(1);
        assertThat(body.get("grant_type").get(0)).isEqualTo("client_credentials");
        assertThat(body.get("client_id").size()).isEqualTo(1);
        assertThat(body.get("client_id").get(0)).isEqualTo("some client id");
        assertThat(body.get("client_secret").size()).isEqualTo(1);
        assertThat(body.get("client_secret").get(0)).isEqualTo("some totp code");
    }

    @Test
    public void shouldThrowExceptionForHttpUnauthorised() {

        HttpClientErrorException response = new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        when(mockRestTemplate.postForEntity(
                anyString(),
                ArgumentMatchers.<Class<HttpEntity<MultiValueMap<String, String>>>>any(),
                ArgumentMatchers.<Class<AccessCodeHmrc>>any()))
                .thenThrow(response);

        assertThatThrownBy(() -> {
            hmrcClient.getAccessCodeFromHmrc("some totp code");
        }).isInstanceOf(ApplicationExceptions.HmrcUnauthorisedException.class);

    }

    @Test
    public void shouldThrowProxyForbiddenExceptionWhenForbiddenFromProxy() {

        HttpClientErrorException response = new HttpClientErrorException(HttpStatus.FORBIDDEN);

        when(mockRestTemplate.postForEntity(
                anyString(),
                ArgumentMatchers.<Class<HttpEntity<MultiValueMap<String, String>>>>any(),
                ArgumentMatchers.<Class<AccessCodeHmrc>>any()))
                .thenThrow(response);

        assertThatThrownBy(() -> hmrcClient.getAccessCodeFromHmrc("some totp code"))
                .isInstanceOf(ApplicationExceptions.ProxyForbiddenException.class);
    }

    @Test
    public void shouldLogWhenAccessCodeResponseReceived() {
        ResponseEntity<AccessCodeHmrc> responseEntity = new ResponseEntity<>(new AccessCodeHmrc("some code", 0, "some token"), HttpStatus.OK);

        when(mockRestTemplate.postForEntity(
                anyString(),
                ArgumentMatchers.<Class<HttpEntity<MultiValueMap<String, String>>>>any(),
                ArgumentMatchers.<Class<AccessCodeHmrc>>any()))
                .thenReturn(responseEntity);

        hmrcClient.getAccessCodeFromHmrc("some totp code");

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Received access code response") &&
                    (loggingEvent.getArgumentArray()[0]).equals(new ObjectAppendingMarker(EVENT, HMRC_GET_ACCESS_CODE_SUCCESS));
        }));
    }
}
