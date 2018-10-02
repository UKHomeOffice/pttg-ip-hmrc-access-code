package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcAccessCodeServiceRuntimeException;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_ACCESS_CODE_REQUEST;
import static uk.gov.digital.ho.pttg.jpa.AccessRepository.ACCESS_ID;

@Component
@Slf4j
class AccessCodeService {

    private final HmrcClient hmrcClient;
    private final int refreshAtMinute;
    private final AccessRepository repository;
    private final AuditClient auditClient;
    private final TotpGenerator totpGenerator;
    private final static int NO_REFRESH = 99;

    AccessCodeService(HmrcClient hmrcClient,
                      @Value("${hmrc.access.code.refresh.at.minute:99}") int refreshAtMinute,
                      AccessRepository accessRepository,
                      AuditClient auditClient, TotpGenerator totpGenerator) {

        this.hmrcClient = hmrcClient;
        this.refreshAtMinute = refreshAtMinute;
        this.repository = accessRepository;
        this.auditClient = auditClient;
        this.totpGenerator = totpGenerator;
    }

    AccessCode getAccessCode() {

        AccessCodeJpa accessCodeJpa = currentAccessCode();

        if (accessCodeJpa.hasExpired()) {
            log.warn("Attempt to get expired Access Code - recreate it first");
            accessCodeJpa = generateAccessCode();
        }

        return new AccessCode(accessCodeJpa.getCode(), accessCodeJpa.getExpiry(), accessCodeJpa.getRefreshTime());
    }

    void refreshAccessCode() {
        generateAccessCode();
        log.info("Access Code refreshed", value(EVENT, HMRC_ACCESS_CODE_REFRESHED));
    }

    void reportUnauthorizedAccessCode(String accessCode) {
        List<AccessCodeJpa> storedAccessCode = repository.findByCode(accessCode);

        if (storedAccessCode.size() > 0) {
            refreshAccessCode();
        }
    }

    private AccessCodeJpa generateAccessCode() {

        auditClient.add(HMRC_ACCESS_CODE_REQUEST);

        log.debug("Obtain new Access Code from HMRC");
        AccessCodeHmrc accessCodeFromHmrc = hmrcClient.getAccessCodeFromHmrc(totpCode());
        log.debug("Obtained new Access Code from HMRC - persist it");

        LocalDateTime expiry = calculateAccessCodeExpiry(accessCodeFromHmrc.getValidDuration());
        LocalDateTime refreshTime = calculateAccessCodeRefreshTime(refreshAtMinute);

        log.debug("Persisting new Access Code with expiry {} and refresh time {}", expiry, refreshTime);
        AccessCodeJpa accessCodeJpa = new AccessCodeJpa(expiry, refreshTime, accessCodeFromHmrc.getCode());

        repository.save(accessCodeJpa);
        log.debug("Persisted new Access Code with expiry {} and refresh time {}", expiry, refreshTime);

        return accessCodeJpa;
    }

    private AccessCodeJpa currentAccessCode() {
        return repository.findById(ACCESS_ID).orElseThrow(() -> new ApplicationExceptions.HmrcRetrieveAccessCodeException(String.format("The database doesn't contain the access code with id %s", ACCESS_ID)));
    }

    private String totpCode() {
        try {
            return totpGenerator.getTotpCode();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Problem generating TOTP code", e, value(EVENT, HMRC_TOTP_GENERATOR_ERROR));
            throw new HmrcAccessCodeServiceRuntimeException("Problem generating TOTP code", e);
        }
    }

    private LocalDateTime calculateAccessCodeExpiry(int validDuration) {
        return LocalDateTime.now().
                plusSeconds(validDuration).         // access codes expires in 4 hours
                minusSeconds(30).                   // clocks may be out by up to 30 seconds for valid TOTP
                minusSeconds(10);                   // allow for a bit more time e.g. process blocked, network latency etc
    }

    private LocalDateTime calculateAccessCodeRefreshTime(int refreshAtMinute) {
        if (refreshAtMinute == NO_REFRESH) {
            return LocalDateTime.now().plusDays(1L);
        }

        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.getMinute() < refreshAtMinute) {
            return currentTime.withMinute(refreshAtMinute);
        } else {
            return currentTime.plusHours(1).withMinute(refreshAtMinute);
        }
    }

}
