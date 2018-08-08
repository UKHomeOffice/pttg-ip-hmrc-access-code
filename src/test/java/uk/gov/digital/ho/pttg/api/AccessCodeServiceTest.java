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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;
import uk.gov.digital.ho.pttg.application.TotpGenerator;
import uk.gov.digital.ho.pttg.audit.AuditClient;
import uk.gov.digital.ho.pttg.hmrc.AccessCodeHmrc;
import uk.gov.digital.ho.pttg.hmrc.HmrcClient;
import uk.gov.digital.ho.pttg.jpa.AccessCodeJpa;
import uk.gov.digital.ho.pttg.jpa.AccessRepository;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_ACCESS_CODE_REQUEST;
import static uk.gov.digital.ho.pttg.jpa.AccessRepository.ACCESS_ID;

@RunWith(MockitoJUnitRunner.class)
public class AccessCodeServiceTest {

    private static final String TOTP_CODE = "IAGVQR33EVGGSZYH";
    private static final int EXPIRES_IN = 3600000;
    private static final String ACCESS_CODE = "access_code";
    private static final String EXISTING_ACCESS_CODE = "existing_access_code";
    private static final int REFRESH_INTERVAL = 360000;
    private static final LocalDateTime JAN_14_2014_19_30 = LocalDateTime.of(2014, Month.JANUARY, 14, 19, 30);

    @Mock private AccessRepository mockRepo;
    @Mock private HmrcClient mockHmrcClient;
    @Mock private AuditClient mockAuditClient;
    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private TotpGenerator mockTotpGenerator;
    @Captor private ArgumentCaptor<AccessCodeJpa> captorAccessCodeJpa;

    private AccessCodeService service;


    @Before
    public void setUp() {

        Logger rootLogger = (Logger) LoggerFactory.getLogger(AccessCodeService.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
        service = new AccessCodeService(mockHmrcClient, REFRESH_INTERVAL, mockRepo, mockAuditClient, mockTotpGenerator);

    }

    @Test
    public void shouldPersistAccessCode() throws InvalidKeyException, NoSuchAlgorithmException {
        when(mockHmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));
        final AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(JAN_14_2014_19_30, EXISTING_ACCESS_CODE);

        adjustAccessCodeCreationDateToAllowRefresh(existingAccessCodeRecord);

        when(mockTotpGenerator.getTotpCode()).thenReturn("access-code");
        when(mockRepo.findById(ACCESS_ID)).thenReturn(Optional.of(existingAccessCodeRecord));

        service.refreshAccessCode();

        verify(mockRepo).save(captorAccessCodeJpa.capture());
        AccessCodeJpa arg = captorAccessCodeJpa.getValue();

        assertThat(arg.getCode()).isEqualTo(ACCESS_CODE);
        assertThat(arg.getExpiry()).isAfter(JAN_14_2014_19_30);
        assertThat(arg.getUpdatedDate()).isAfter(existingAccessCodeRecord.getUpdatedDate());
    }

    @Test
    public void shouldNotPersistAccessCodeIfRecentlyReplaced(){

        AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(JAN_14_2014_19_30, EXISTING_ACCESS_CODE);

        when(mockRepo.findById(ACCESS_ID)).thenReturn(Optional.of(existingAccessCodeRecord));

        service.refreshAccessCode();

        verify(mockRepo, never()).save(any(AccessCodeJpa.class));
        verifyZeroInteractions(mockHmrcClient);
    }

    @Test
    public void shouldRetrieveAccessCodeWithoutTriggeringRefresh(){
        final AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(LocalDateTime.now().plusMinutes(30), EXISTING_ACCESS_CODE);
        when(mockRepo.findById(ACCESS_ID)).thenReturn(Optional.of(existingAccessCodeRecord));

        assertThat(service.getAccessCode()).hasFieldOrPropertyWithValue("code", EXISTING_ACCESS_CODE);

        verify(mockHmrcClient, never()).getAccessCodeFromHmrc(anyString());
    }

    @Test
    public void shouldRetrieveAccessCodeTriggeringRefreshAsExpired() throws InvalidKeyException, NoSuchAlgorithmException {

        when(mockHmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));
        final AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(JAN_14_2014_19_30, EXISTING_ACCESS_CODE);

        when(mockTotpGenerator.getTotpCode()).thenReturn("access-code");
        when(mockRepo.findById(ACCESS_ID)).thenReturn(Optional.of(existingAccessCodeRecord));

        assertThat(service.getAccessCode()).hasFieldOrPropertyWithValue("code", ACCESS_CODE);

        verify(mockRepo).save(captorAccessCodeJpa.capture());
        AccessCodeJpa arg = captorAccessCodeJpa.getValue();

        assertThat(arg.getCode()).isEqualTo(ACCESS_CODE);
        assertThat(arg.getExpiry()).isAfter(JAN_14_2014_19_30);
        assertThat(arg.getUpdatedDate()).isAfter(existingAccessCodeRecord.getUpdatedDate());
    }

    @Test
    public void shouldAudit() throws InvalidKeyException, NoSuchAlgorithmException {
        AccessCodeJpa mockAccessCodeJpa = mock(AccessCodeJpa.class);
        when(mockAccessCodeJpa.getUpdatedDate()).thenReturn(LocalDateTime.now().minusDays(1));

        when(mockTotpGenerator.getTotpCode()).thenReturn("access-code");
        when(mockRepo.findById(ACCESS_ID)).thenReturn(Optional.of(mockAccessCodeJpa));
        when(mockHmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));
        service.refreshAccessCode();

        verify(mockAuditClient).add(HMRC_ACCESS_CODE_REQUEST);
    }


    @Test
    public void shouldLogAccessCodeRefreshed() throws InvalidKeyException, NoSuchAlgorithmException {
        AccessCodeJpa mockAccessCodeJpa = mock(AccessCodeJpa.class);
        when(mockAccessCodeJpa.getUpdatedDate()).thenReturn(LocalDateTime.now().minusDays(1));

        when(mockTotpGenerator.getTotpCode()).thenReturn("access-code");
        when(mockRepo.findById(ACCESS_ID)).thenReturn(Optional.of(mockAccessCodeJpa));
        when(mockHmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));

        service.refreshAccessCode();

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Access Code refreshed") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogTOTPFailure() throws InvalidKeyException, NoSuchAlgorithmException {


        final AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(JAN_14_2014_19_30, EXISTING_ACCESS_CODE);

        adjustAccessCodeCreationDateToAllowRefresh(existingAccessCodeRecord);

        when(mockRepo.findById(ACCESS_ID)).thenReturn(Optional.of(existingAccessCodeRecord));
        when(mockTotpGenerator.getTotpCode()).thenThrow(new InvalidKeyException());

        assertThatThrownBy(() -> service.refreshAccessCode()).isInstanceOf(ApplicationExceptions.HmrcAccessCodeServiceRuntimeException.class);;

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Problem generating TOTP code") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    /* refresh will only occur if the access code hasn't been recently refreshed
       recently = half of the refresh interval,  if interval = 1hr then refresh request will be ignored if within 30 mins of previous refresh
     */
    private void adjustAccessCodeCreationDateToAllowRefresh(AccessCodeJpa existingAccessCodeRecord) {
        PropertyAccessor myAccessor = PropertyAccessorFactory.forDirectFieldAccess(existingAccessCodeRecord);
        myAccessor.setPropertyValue("updatedDate", LocalDateTime.now().minus(REFRESH_INTERVAL, ChronoUnit.MILLIS));
    }
}