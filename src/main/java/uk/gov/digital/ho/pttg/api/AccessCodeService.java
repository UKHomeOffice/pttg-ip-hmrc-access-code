package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.TotpGenerator;
import uk.gov.digital.ho.pttg.hmrc.AccessCodeHmrc;
import uk.gov.digital.ho.pttg.hmrc.HmrcClient;
import uk.gov.digital.ho.pttg.jpa.AccessCodeJpa;
import uk.gov.digital.ho.pttg.jpa.AccessRepository;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcAccessCodeServiceRuntimeException;
import static uk.gov.digital.ho.pttg.jpa.AccessRepository.ACCESS_ID;

@Component
@Slf4j
class AccessCodeService {

    private final HmrcClient hmrcClient;
    private final String totpKey;

    private final int refreshInterval;
    private AccessRepository repository;

    @Autowired
    public AccessCodeService(HmrcClient hmrcClient,
                             @Value("${totp.key}") String totpKey,
                             @Value("${refresh.interval}") int refreshInterval,
                             AccessRepository accessRepository) {
        this.hmrcClient = hmrcClient;
        this.totpKey = totpKey;
        this.refreshInterval = refreshInterval;
        this.repository = accessRepository;
    }

    AccessCode getAccessCode() {

        final AccessCodeJpa accessCodeJpa = getPersistedAccessCode();
        return new AccessCode(accessCodeJpa.getCode(), accessCodeJpa.getExpiry());
    }

    void refreshAccessCode() {
        if (accessCodeShouldBeRefreshed()) {
            final AccessCodeHmrc accessCodeFromHmrc = hmrcClient.getAccessCodeFromHmrc(getTotpCode());
            persistOnSuccessfulRetrieval(accessCodeFromHmrc);
        } else {
            log.info("Ignoring refresh request -  access code was refreshed at the last {} minutes", TimeUnit.MILLISECONDS.toMinutes(refreshInterval));
        }
    }

    private void persistOnSuccessfulRetrieval(AccessCodeHmrc accessCodeFromHmrc) {
        if (accessCodeFromHmrc !=null) {
            LocalDateTime expiry = calculateAccessCodeExpiry(accessCodeFromHmrc.getValidDuration());
            repository.save(new AccessCodeJpa(expiry, accessCodeFromHmrc.getCode()));
        }
    }

    private boolean accessCodeShouldBeRefreshed() {
        return getPersistedAccessCode().getUpdatedDate().isBefore(LocalDateTime.now().minus((refreshInterval/2), ChronoUnit.MILLIS));
    }

    private AccessCodeJpa getPersistedAccessCode() {
        return repository.findOne(ACCESS_ID);
    }

    private String getTotpCode() {
        try {
            return TotpGenerator.getTotpCode(totpKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Problem generating TOTP code", e);
            throw new HmrcAccessCodeServiceRuntimeException("Problem generating TOTP code", e);
        }
    }

    private LocalDateTime calculateAccessCodeExpiry(int validDuration) {
        return LocalDateTime.now().
                plusSeconds(validDuration).         // access codes expires in 4 hours
                minusSeconds(30).                   // clocks may be out by up to 30 seconds for valid TOTP
                minusSeconds(10);                   // allow for a bit more time e.g. process blocked, network latency etc
    }
}
