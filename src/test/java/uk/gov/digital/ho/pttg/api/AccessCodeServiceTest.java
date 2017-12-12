package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import uk.gov.digital.ho.pttg.audit.AuditClient;
import uk.gov.digital.ho.pttg.hmrc.AccessCodeHmrc;
import uk.gov.digital.ho.pttg.hmrc.HmrcClient;
import uk.gov.digital.ho.pttg.jpa.AccessCodeJpa;
import uk.gov.digital.ho.pttg.jpa.AccessRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Captor private ArgumentCaptor<AccessCodeJpa> captorAccessCodeJpa;

    private AccessCodeService service;

    @Before
    public void setUp() throws Exception {
        service = new AccessCodeService(mockHmrcClient, TOTP_CODE, REFRESH_INTERVAL, mockRepo, mockAuditClient);
    }

    @Test
    public void shouldPersistAccessCode(){
        when(mockHmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));
        final AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(JAN_14_2014_19_30, EXISTING_ACCESS_CODE);

        adjustAccessCodeCreationDateToAllowRefresh(existingAccessCodeRecord);

        when(mockRepo.findOne(ACCESS_ID)).thenReturn(existingAccessCodeRecord);

        service.refreshAccessCode();

        verify(mockRepo).save(captorAccessCodeJpa.capture());
        AccessCodeJpa arg = captorAccessCodeJpa.getValue();

        assertThat(arg.getCode()).isEqualTo(ACCESS_CODE);
        assertThat(arg.getExpiry()).isAfter(JAN_14_2014_19_30);
        assertThat(arg.getUpdatedDate()).isAfter(existingAccessCodeRecord.getUpdatedDate());
    }

    @Test
    public void shouldNotPersistAccessCodeIfRecentlyReplaced(){
        when(mockHmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));
        final AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(JAN_14_2014_19_30, EXISTING_ACCESS_CODE);
        when(mockRepo.findOne(ACCESS_ID)).thenReturn(existingAccessCodeRecord);

        service.refreshAccessCode();

        verify(mockRepo, never()).save(any(AccessCodeJpa.class));
    }

    @Test
    public void shouldRetrieveAccessCodeWithoutTriggeringRefresh(){
        final AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(LocalDateTime.now().plusMinutes(30), EXISTING_ACCESS_CODE);
        when(mockRepo.findOne(ACCESS_ID)).thenReturn(existingAccessCodeRecord);

        assertThat(service.getAccessCode()).hasFieldOrPropertyWithValue("code", EXISTING_ACCESS_CODE);

        verify(mockHmrcClient, never()).getAccessCodeFromHmrc(anyString());
    }

    @Test
    public void shouldRetrieveAccessCodeTriggeringRefreshAsExpired(){

        when(mockHmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));
        final AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(JAN_14_2014_19_30, EXISTING_ACCESS_CODE);

        when(mockRepo.findOne(ACCESS_ID)).thenReturn(existingAccessCodeRecord);

        assertThat(service.getAccessCode()).hasFieldOrPropertyWithValue("code", ACCESS_CODE);

        verify(mockRepo).save(captorAccessCodeJpa.capture());
        AccessCodeJpa arg = captorAccessCodeJpa.getValue();

        assertThat(arg.getCode()).isEqualTo(ACCESS_CODE);
        assertThat(arg.getExpiry()).isAfter(JAN_14_2014_19_30);
        assertThat(arg.getUpdatedDate()).isAfter(existingAccessCodeRecord.getUpdatedDate());
    }

    @Test
    public void shouldAudit() {
        AccessCodeJpa mockAccessCodeJpa = mock(AccessCodeJpa.class);
        when(mockAccessCodeJpa.getUpdatedDate()).thenReturn(LocalDateTime.now().minusDays(1));

        when(mockRepo.findOne(ACCESS_ID)).thenReturn(mockAccessCodeJpa);
        when(mockHmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(null);

        service.refreshAccessCode();

        verify(mockAuditClient).add(HMRC_ACCESS_CODE_REQUEST);
    }

    /* refresh will only occur if the access code hasn't been recently refreshed
       recently = half of the refresh interval,  if interval = 1hr then refresh request will be ignored if within 30 mins of previous refresh
     */
    private void adjustAccessCodeCreationDateToAllowRefresh(AccessCodeJpa existingAccessCodeRecord) {
        PropertyAccessor myAccessor = PropertyAccessorFactory.forDirectFieldAccess(existingAccessCodeRecord);
        myAccessor.setPropertyValue("updatedDate", LocalDateTime.now().minus(REFRESH_INTERVAL, ChronoUnit.MILLIS));
    }
}