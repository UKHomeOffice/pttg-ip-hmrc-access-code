package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.application.TotpGenerator;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcAccessCodeServiceRuntimeException;

@Component
@Slf4j
class AccessCodeGenerator {

    private LocalDateTime expiry = LocalDateTime.of(0,1,1,0,0);
    private AccessCode accessCode = new AccessCode("NOT SET YET", 0, "NOT SET YET");

    private RestTemplate restTemplate;
    private String url;
    private String clientId;
    private final String totpKey;

    @Autowired
    public AccessCodeGenerator(RestTemplate restTemplate,
                               @Value("${hmrc.endpoint}") String url,
                               @Value("${client.id}") String clientId,
                               @Value("${totp.key}") String totpKey) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.clientId = clientId;
        this.totpKey = totpKey;
    }

    synchronized AccessCode getAccessCode(LocalDateTime now) {

        if (now.isAfter(expiry)) {
            log.info("Current access code expired at {} - Ask HMRC for next access code", expiry);
            accessCode = getAccessCode();
            expiry = calculateAccessCodeExpiry(accessCode.getValidDuration());
        } else {
            log.info("Current access code is valid until {}", expiry);
        }

        return accessCode;
    }


    AccessCode getAccessCode() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        final String totpCode = getTotpCode();

        map.add("grant_type", "client_credentials");
        map.add("client_id", clientId);
        map.add("client_secret", totpCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        log.info("Calling HMRC for new oauth/token");

        final AccessCode accessCode = restTemplate.postForEntity(url + "/oauth/token", request, AccessCode.class).getBody();

        log.info("Received AuthToken response");

        return accessCode;
    }

    String getTotpCode() {
        try {
            return TotpGenerator.getTotpCode(totpKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Problem generating TOTP code", e);
            throw new HmrcAccessCodeServiceRuntimeException("Problem generating TOTP code", e);
        }
    }

    LocalDateTime calculateAccessCodeExpiry(int validDuration) {
        return LocalDateTime.now().
                plusSeconds(validDuration).         // access codes expires in 4 hours
                minusSeconds(30).                   // clocks may be out by up to 30 seconds for valid TOTP
                minusSeconds(10);                   // allow for a bit more time e.g. process blocked, network latency etc
    }
}
