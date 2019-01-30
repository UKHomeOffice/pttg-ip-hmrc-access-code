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
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_ACCESS_CODE_RESPONSE_SUCCESS;


@Component
@Slf4j
public class HmrcClient {

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String accessTokenResource;

    @Autowired
    HmrcClient(@Qualifier(value = "hmrcRestTemplate") RestTemplate restTemplate,
                      @Value("${client.id}") String clientId,
                      @Value("${hmrc.endpoint}") String baseHmrcUrl) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.accessTokenResource = baseHmrcUrl + "/oauth/token";
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

        log.debug("Calling HMRC for new access code");

        try {
            accessCode = restTemplate.postForEntity(accessTokenResource, request, AccessCodeHmrc.class).getBody();
            if(accessCode == null) {
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

        log.info("Received access code response", value(EVENT, HMRC_ACCESS_CODE_RESPONSE_SUCCESS));
        return accessCode;
    }
}
