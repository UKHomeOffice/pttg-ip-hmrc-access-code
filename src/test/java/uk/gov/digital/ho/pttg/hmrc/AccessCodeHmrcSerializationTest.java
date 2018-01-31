package uk.gov.digital.ho.pttg.hmrc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

public class AccessCodeHmrcSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldUnmarshall() throws IOException {

        String hmrcResponse = "{\"access_token\": \"some access token\", \"expires_in\": 0, \"refresh_token\": \"some refresh token\"}";

        AccessCodeHmrc accessCode = mapper.readValue(hmrcResponse, AccessCodeHmrc.class);

        Assertions.assertThat(accessCode.getCode()).isEqualTo("some access token");
        Assertions.assertThat(accessCode.getValidDuration()).isEqualTo(0);
        Assertions.assertThat(accessCode.getRefreshToken()).isEqualTo("some refresh token");
    }

    @Test
    public void shouldMarshall() throws JsonProcessingException, JSONException {

        String expectedJson = "{\"code\": \"some code\", \"validDuration\": 0, \"refreshToken\": \"some refresh token\"}";

        AccessCodeHmrc accessCode = new AccessCodeHmrc("some code", 0, "some refresh token");

        String jsonFromObject = mapper.writeValueAsString(accessCode);

        JSONAssert.assertEquals(jsonFromObject, expectedJson, false);
    }
}