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
import org.springframework.test.util.ReflectionTestUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_ACCESS_CODE_REQUEST;
import static uk.gov.digital.ho.pttg.jpa.AccessRepository.ACCESS_ID;

@RunWith(MockitoJUnitRunner.class)
public class AccessCodeServiceTest {

    private static final int EXPIRES_IN = 3600000;
    private static final String ACCESS_CODE = "access_code";
    private static final String EXISTING_ACCESS_CODE = "existing_access_code";
    private static final LocalDateTime JAN_14_2014_19_30 = LocalDateTime.of(2014, Month.JANUARY, 14, 19, 30);
    private static final LocalDateTime JAN_14_2014_16_30 = LocalDateTime.of(2014, Month.JANUARY, 14, 16, 30);
    private static final int REFRESH_AT_MINUTE = 99;

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
        service = new AccessCodeService(mockHmrcClient, REFRESH_AT_MINUTE, mockRepo, mockAuditClient, mockTotpGenerator);

    }

    @Test
    public void shouldPersistAccessCode() throws InvalidKeyException, NoSuchAlgorithmException {
        when(mockHmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));

        when(mockTotpGenerator.getTotpCode()).thenReturn("access-code");

        service.refreshAccessCode();

        verify(mockRepo).save(captorAccessCodeJpa.capture());
        AccessCodeJpa arg = captorAccessCodeJpa.getValue();

        assertThat(arg.getCode()).isEqualTo(ACCESS_CODE);
        assertThat(arg.getExpiry()).isAfter(JAN_14_2014_19_30);
    }

    @Test
    public void shouldRetrieveAccessCodeWithoutTriggeringRefresh(){
        final AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(LocalDateTime.now().plusMinutes(30), LocalDateTime.now().minusMinutes(30), EXISTING_ACCESS_CODE);
        when(mockRepo.findById(ACCESS_ID)).thenReturn(Optional.of(existingAccessCodeRecord));

        assertThat(service.getAccessCode()).hasFieldOrPropertyWithValue("code", EXISTING_ACCESS_CODE);

        verify(mockHmrcClient, never()).getAccessCodeFromHmrc(anyString());
    }

    @Test
    public void shouldRetrieveAccessCodeTriggeringRefreshAsExpired() throws InvalidKeyException, NoSuchAlgorithmException {

        when(mockHmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));
        final AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(JAN_14_2014_19_30, JAN_14_2014_16_30, EXISTING_ACCESS_CODE);

        when(mockTotpGenerator.getTotpCode()).thenReturn("access-code");
        when(mockRepo.findById(ACCESS_ID)).thenReturn(Optional.of(existingAccessCodeRecord));

        assertThat(service.getAccessCode()).hasFieldOrPropertyWithValue("code", ACCESS_CODE);

        verify(mockRepo).save(captorAccessCodeJpa.capture());
        AccessCodeJpa arg = captorAccessCodeJpa.getValue();

        assertThat(arg.getCode()).isEqualTo(ACCESS_CODE);
        assertThat(arg.getExpiry()).isAfter(JAN_14_2014_19_30);
        assertThat(arg.getRefreshTime()).isAfter(JAN_14_2014_19_30);
        assertThat(arg.getUpdatedDate()).isAfter(existingAccessCodeRecord.getUpdatedDate());
    }

    @Test
    public void shouldApplyRefreshAtMinuteToAccessCode() throws InvalidKeyException, NoSuchAlgorithmException {
        ReflectionTestUtils.setField(service, "refreshAtMinute", 10);
        when(mockHmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));
        when(mockTotpGenerator.getTotpCode()).thenReturn("access-code");

        service.refreshAccessCode();

        verify(mockRepo).save(captorAccessCodeJpa.capture());
        AccessCodeJpa savedAccessCode = captorAccessCodeJpa.getValue();

        assertThat(savedAccessCode.getRefreshTime().getMinute()).isEqualTo(10);

        ReflectionTestUtils.setField(service, "refreshAtMinute", REFRESH_AT_MINUTE);
    }

    @Test
    public void shouldAudit() throws InvalidKeyException, NoSuchAlgorithmException {
        when(mockTotpGenerator.getTotpCode()).thenReturn("access-code");
        when(mockHmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));
        service.refreshAccessCode();

        verify(mockAuditClient).add(HMRC_ACCESS_CODE_REQUEST);
    }


    @Test
    public void shouldLogAccessCodeRefreshed() throws InvalidKeyException, NoSuchAlgorithmException {
        when(mockTotpGenerator.getTotpCode()).thenReturn("access-code");
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
        when(mockTotpGenerator.getTotpCode()).thenThrow(new InvalidKeyException());

        assertThatThrownBy(() -> service.refreshAccessCode()).isInstanceOf(ApplicationExceptions.HmrcAccessCodeServiceRuntimeException.class);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Problem generating TOTP code") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldNotRefreshReportedAccessCodeIfNotStored() {
        when(mockRepo.findByCode(anyString())).thenReturn(new ArrayList<>());

        service.reportUnauthorizedAccessCode("some access code");

        verifyZeroInteractions(mockHmrcClient);
        verify(mockRepo, never()).save(any(AccessCodeJpa.class));
    }

    @Test
    public void shouldRefreshReportedAccessCodeIfStored() throws InvalidKeyException, NoSuchAlgorithmException {
        String code = "unauthorized access code";
        AccessCodeJpa storedAccessCode = new AccessCodeJpa(LocalDateTime.MAX, LocalDateTime.MAX, code);
        when(mockRepo.findByCode(code)).thenReturn(Arrays.asList(storedAccessCode));
        when(mockTotpGenerator.getTotpCode()).thenReturn("access-code");
        when(mockHmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));

        service.reportUnauthorizedAccessCode(code);

        verify(mockHmrcClient).getAccessCodeFromHmrc(anyString());
        verify(mockRepo).save(any(AccessCodeJpa.class));
    }

}