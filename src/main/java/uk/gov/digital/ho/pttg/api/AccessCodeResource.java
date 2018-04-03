package uk.gov.digital.ho.pttg.api;

import com.codahale.metrics.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AccessCodeResource {

    private final AccessCodeService accessCodeService;

    @Autowired
    public AccessCodeResource(AccessCodeService accessCodeService) {
        this.accessCodeService = accessCodeService;
    }

    @Timed
    @GetMapping(path = "/access", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AccessCode> getAccessCode() {

        log.info("Calling AccessCodeService to produce new access code");

        AccessCode accessCode = accessCodeService.getAccessCode();

        log.info("AccessCodeService produced new access code");

        return ResponseEntity.ok(accessCode);
    }

    @Timed
    @PostMapping(path = "/refresh")
    @ResponseStatus(value = HttpStatus.OK)
    public void refresh() {

        log.info("Calling AccessCodeService to refresh access code");

        accessCodeService.refreshAccessCode();

        log.info("AccessCodeService refreshed access code");
    }
}