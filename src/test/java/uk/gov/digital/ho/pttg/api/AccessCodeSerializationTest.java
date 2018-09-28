package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.digital.ho.pttg.application.SpringConfiguration;

import java.time.LocalDateTime;
import java.time.Month;


public class AccessCodeSerializationTest {

    private ObjectMapper mapper = new ObjectMapper();
    private LocalDateTime JAN_14_2014_19_30 = LocalDateTime.of(2014, Month.JANUARY, 14, 19, 30);
    private LocalDateTime JAN_14_2014_16_30 = LocalDateTime.of(2014, Month.JANUARY, 14, 16, 30);


    @Test
    public void shouldMarshall() throws JsonProcessingException, JSONException {
        SpringConfiguration.initialiseObjectMapper(mapper);

        String expectedJson = "{\"code\": \"some code\", \"expiry\": \"2014-01-14T19:30:00\"}";

        AccessCode accessCode = new AccessCode("some code", JAN_14_2014_19_30, JAN_14_2014_16_30);

        String jsonFromObject = mapper.writeValueAsString(accessCode);

        JSONAssert.assertEquals(expectedJson, jsonFromObject, false);
    }
}