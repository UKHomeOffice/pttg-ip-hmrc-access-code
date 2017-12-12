package uk.gov.digital.ho.pttg.hmrc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Component
@Slf4j
public class HmrcClient {

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String url;

    @Autowired
    public HmrcClient(RestTemplate restTemplate,
                      @Value("${client.id}") String clientId,
                      @Value("${hmrc.endpoint}") String url) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.url = url;
    }

    public AccessCodeHmrc getAccessCodeFromHmrc(String totpCode) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        AccessCodeHmrc accessCode;

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("grant_type", "client_credentials");
        map.add("client_id", clientId);
        map.add("client_secret", totpCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        log.info("Calling HMRC for new access code");

        try {
            accessCode = restTemplate.postForEntity(url + "/oauth/token", request, AccessCodeHmrc.class).getBody();
        } catch (RestClientException e) {
            log.error("Problem retrieving Access Code from HMRC", e);
            throw new ApplicationExceptions.HmrcAccessCodeServiceRuntimeException("Problem retrieving Access Code from HMRC", e);
        }

        log.info("Received access code response");

        return accessCode;
    }
}
