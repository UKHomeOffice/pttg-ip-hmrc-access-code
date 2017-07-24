package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class AccessCodeResourceTest {

    private AccessCodeResource resource;

    @Before
    public void setup() {
        resource = new AccessCodeResource();
    }

    @Test
    public void shouldReturnStaticResponse() throws IOException {

        ResponseEntity response = resource.getCurrentAccessCode();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(((AccessCode)response.getBody()).getCode()).isEqualTo("STUB access code");
    }
}