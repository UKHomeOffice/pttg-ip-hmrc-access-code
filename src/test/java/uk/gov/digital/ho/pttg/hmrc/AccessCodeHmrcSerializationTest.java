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

        String hmrcResponse = "{\"access_token\": \"some access token\"}";

        AccessCodeHmrc accessCode = mapper.readValue(hmrcResponse, AccessCodeHmrc.class);

        Assertions.assertThat(accessCode.getCode()).isEqualTo("some access token");
    }

    @Test
    public void shouldMarshall() throws JsonProcessingException, JSONException {

        String expectedJson = "{\"code\": \"some code\"}";

        AccessCodeHmrc accessCode = new AccessCodeHmrc("some code", 0, "some refresh token");

        String jsonFromObject = mapper.writeValueAsString(accessCode);

        JSONAssert.assertEquals(jsonFromObject, expectedJson, false);
    }
}