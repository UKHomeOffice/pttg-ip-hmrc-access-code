package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

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

        long requestSent = getTimestamp();

        log.info("Calling AccessCodeService to produce new access code", value(EVENT, HMRC_ACCESS_CODE_REQUESTED));

        AccessCode accessCode = accessCodeService.getAccessCode();

        long duration = getDuration(requestSent);

        log.info("AccessCodeService returned access code",
                value(EVENT, HMRC_ACCESS_CODE_RESPONSE_SUCCESS),
                value("request_duration_ms", duration))
        ;

        return ResponseEntity.ok(accessCode);
    }

    @PostMapping(path = "/refresh")
    @ResponseStatus(value = HttpStatus.OK)
    public void refresh() {

        long requestSent = getTimestamp();

        log.info("Calling AccessCodeService to refresh access code", value(EVENT, HMRC_ACCESS_CODE_REFRESH_REQUESTED));

        accessCodeService.refreshAccessCode();

        long duration = getDuration(requestSent);

        log.info("AccessCodeService refreshed access code",
                value(EVENT, HMRC_ACCESS_CODE_REFRESH_RESPONSE_SUCCESS),
                value("request_duration_ms", duration))
        ;
    }

    @PostMapping(path = "/access/{accessCode}/report")
    @ResponseStatus(value = HttpStatus.OK)
    public void report(@PathVariable String accessCode) {

        long requestSent = getTimestamp();

        log.info("Calling AccessCodeService to report unauthorized access code", value(EVENT, HMRC_ACCESS_CODE_REPORTED));

        accessCodeService.reportUnauthorizedAccessCode(accessCode);

        long duration = getDuration(requestSent);

        log.info("AccessCodeService reported unauthorized access code",
                value(EVENT, HMRC_ACCESS_CODE_REPORTED_RESPONSE_SUCCESS),
                value("request_duration_ms", duration));
    }

    private long getDuration(long whenRequestWasSent) {
        return getTimestamp() - whenRequestWasSent;
    }

    private long getTimestamp() {
        return Instant.now().toEpochMilli();
    }
}