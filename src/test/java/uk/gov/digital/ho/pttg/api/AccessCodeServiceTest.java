package uk.gov.digital.ho.pttg.api;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import uk.gov.digital.ho.pttg.hmrc.AccessCodeHmrc;
import uk.gov.digital.ho.pttg.hmrc.HmrcClient;
import uk.gov.digital.ho.pttg.jpa.AccessCodeJpa;
import uk.gov.digital.ho.pttg.jpa.AccessRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccessCodeServiceTest {
    private static final String TOTP_CODE = "IAGVQR33EVGGSZYH";
    private static final int EXPIRES_IN = 3600000;
    private static final String ACCESS_CODE = "access_code";
    private static final String EXISTING_ACCESS_CODE = "existing_access_code";
    private static final int REFRESH_INTERVAL = 360000;
    private LocalDateTime JAN_14_2014_19_30 = LocalDateTime.of(2014, Month.JANUARY, 14, 19, 30);

    @Mock
    private AccessRepository mockRepo;

    @Mock
    private HmrcClient mockClient;

    @Captor
    private ArgumentCaptor<AccessCodeJpa> captorAccessCodeJpa;

    private AccessCodeService service;

    @Before
    public void setUp() throws Exception {
        service = new AccessCodeService(mockClient, TOTP_CODE, REFRESH_INTERVAL, mockRepo);
    }

    @Test
    public void shouldPersistAccessCode(){
        when(mockClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));
        final AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(JAN_14_2014_19_30, EXISTING_ACCESS_CODE);

        adjustAccessCodeCreationDateToAllowRefresh(existingAccessCodeRecord);

        when(mockRepo.findOne(AccessRepository.ACCESS_ID)).thenReturn(existingAccessCodeRecord);

        service.refreshAccessCode();

        verify(mockRepo).save(captorAccessCodeJpa.capture());
        AccessCodeJpa arg = captorAccessCodeJpa.getValue();

        assertThat(arg.getCode()).isEqualTo(ACCESS_CODE);
        assertThat(arg.getExpiry()).isAfter(JAN_14_2014_19_30);
        assertThat(arg.getUpdatedDate()).isAfter(existingAccessCodeRecord.getUpdatedDate());
    }

    @Test
    public void shouldNotPersistAccessCodeIfRecentlyReplaced(){
        when(mockClient.getAccessCodeFromHmrc(anyString())).thenReturn(new AccessCodeHmrc(ACCESS_CODE, EXPIRES_IN, "refresh_token"));
        final AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(JAN_14_2014_19_30, EXISTING_ACCESS_CODE);
        when(mockRepo.findOne(AccessRepository.ACCESS_ID)).thenReturn(existingAccessCodeRecord);

        service.refreshAccessCode();

        verify(mockRepo, never()).save(any(AccessCodeJpa.class));
    }

    @Test
    public void shouldRetrieveAccessCode(){
        final AccessCodeJpa existingAccessCodeRecord = new AccessCodeJpa(JAN_14_2014_19_30, EXISTING_ACCESS_CODE);
        when(mockRepo.findOne(AccessRepository.ACCESS_ID)).thenReturn(existingAccessCodeRecord);

        Assertions.assertThat(service.getAccessCode()).isEqualTo(new AccessCode(EXISTING_ACCESS_CODE, JAN_14_2014_19_30));

        verify(mockClient, never()).getAccessCodeFromHmrc(anyString());
    }

    /* refresh will only occur if the access code hasn't been recently refreshed
       recently = half of the refresh interval,  if interval = 1hr then refresh request will be ignored if within 30 mins of previous refresh
     */
    private void adjustAccessCodeCreationDateToAllowRefresh(AccessCodeJpa existingAccessCodeRecord) {
        PropertyAccessor myAccessor = PropertyAccessorFactory.forDirectFieldAccess(existingAccessCodeRecord);
        myAccessor.setPropertyValue("updatedDate", LocalDateTime.now().minus(REFRESH_INTERVAL, ChronoUnit.MILLIS));
    }
}