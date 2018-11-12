package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;
import static uk.gov.digital.ho.pttg.api.RequestData.REQUEST_DURATION_MS;


@RestController
@Slf4j
class AccessCodeResource {

    private final AccessCodeService accessCodeService;
    private final RequestData requestHeaderData;

    @Autowired
    AccessCodeResource(AccessCodeService accessCodeService, RequestData requestHeaderData) {
        this.accessCodeService = accessCodeService;
        this.requestHeaderData = requestHeaderData;
    }

    @GetMapping(path = "/access", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AccessCode> getAccessCode() {

        log.info("Calling AccessCodeService to produce new access code", value(EVENT, HMRC_ACCESS_CODE_REQUESTED));

        AccessCode accessCode = accessCodeService.getAccessCode();

        log.info("AccessCodeService returned access code",
                value(EVENT, HMRC_ACCESS_CODE_RESPONSE_SUCCESS),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));

        return ResponseEntity.ok(accessCode);
    }

    @PostMapping(path = "/refresh")
    @ResponseStatus(value = HttpStatus.OK)
    void refresh() {

        log.info("Calling AccessCodeService to refresh access code", value(EVENT, HMRC_ACCESS_CODE_REFRESH_REQUESTED));

        accessCodeService.refreshAccessCode();

        log.info("AccessCodeService refreshed access code",
                value(EVENT, HMRC_ACCESS_CODE_REFRESH_RESPONSE_SUCCESS),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()))
        ;
    }

    @PostMapping(path = "/access/{accessCode}/report")
    @ResponseStatus(value = HttpStatus.OK)
    void report(@PathVariable String accessCode) {

        log.info("Calling AccessCodeService to report unauthorized access code", value(EVENT, HMRC_ACCESS_CODE_REPORTED));

        accessCodeService.reportUnauthorizedAccessCode(accessCode);

        log.info("AccessCodeService reported unauthorized access code",
                value(EVENT, HMRC_ACCESS_CODE_REPORTED_RESPONSE_SUCCESS),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
    }
}