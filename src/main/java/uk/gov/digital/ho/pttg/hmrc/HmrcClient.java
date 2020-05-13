package uk.gov.digital.ho.pttg.hmrc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcAccessCodeServiceRuntimeException;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_GET_ACCESS_CODE_SUCCESS;

@Component
@Slf4j
public class HmrcClient {

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;
    private final String accessTokenResource;

    @Autowired
    HmrcClient(@Qualifier(value = "hmrcRestTemplate") RestTemplate restTemplate,
               @Value("${client.id}") String clientId,
               @Value("${client.secret}") String clientSecret,
               @Value("${hmrc.endpoint}") String baseHmrcUrl) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessTokenResource = baseHmrcUrl + "/oauth/token";
    }

    public AccessCodeHmrc getAccessCodeFromHmrc(String totpCode) {

        AccessCodeHmrc accessCode;

        try {
            accessCode = restTemplate.postForEntity(accessTokenResource, generateRequest(totpCode), AccessCodeHmrc.class).getBody();
            if (accessCode == null) {
                throw new HmrcAccessCodeServiceRuntimeException("HMRC returned null access code");
            }
        } catch (HttpClientErrorException ex) {
            HttpStatus statusCode = ex.getStatusCode();
            if (statusCode.equals(FORBIDDEN)) {
                throw new ApplicationExceptions.ProxyForbiddenException("Received a 403 Forbidden response from proxy");
            } else if (statusCode.equals(UNAUTHORIZED)) {
                throw new ApplicationExceptions.HmrcUnauthorisedException(ex.getMessage(), ex);
            } else {
                throw new HmrcAccessCodeServiceRuntimeException("Problem retrieving Access Code from HMRC", ex);
            }
        }

        log.info("Received access code response", value(EVENT, HMRC_GET_ACCESS_CODE_SUCCESS));
        return accessCode;
    }

    private HttpEntity<MultiValueMap<String, String>> generateRequest(String totpCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> oauthData = new LinkedMultiValueMap<>();

        oauthData.add("grant_type", "client_credentials");
        oauthData.add("client_id", clientId);
        oauthData.add("client_secret", totpCode + clientSecret);

        return new HttpEntity<>(oauthData, headers);
    }
}
