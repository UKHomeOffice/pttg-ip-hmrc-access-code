package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;


@RestController
@Slf4j
public class AccessCodeResource {

    private final AccessCodeService accessCodeService;

    @Autowired
    public AccessCodeResource(AccessCodeService accessCodeService) {
        this.accessCodeService = accessCodeService;
    }

    @GetMapping(path = "/access", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AccessCode> getAccessCode() {

        log.info("Calling AccessCodeService to produce new access code", value(EVENT, HMRC_ACCESS_CODE_REQUESTED));

        AccessCode accessCode = accessCodeService.getAccessCode();

        log.info("AccessCodeService returned access code", value(EVENT, HMRC_ACCESS_CODE_RESPONSE_SUCCESS));

        return ResponseEntity.ok(accessCode);
    }

    @PostMapping(path = "/refresh")
    @ResponseStatus(value = HttpStatus.OK)
    public void refresh() {

        log.info("Calling AccessCodeService to refresh access code", value(EVENT, HMRC_ACCESS_CODE_REFRESH_REQUESTED));

        accessCodeService.refreshAccessCode();

        log.info("AccessCodeService refreshed access code", value(EVENT, HMRC_ACCESS_CODE_REFRESH_RESPONSE_SUCCESS));
    }
}