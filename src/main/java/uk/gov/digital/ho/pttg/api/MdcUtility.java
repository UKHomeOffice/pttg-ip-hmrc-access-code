package uk.gov.digital.ho.pttg.api;

import org.springframework.stereotype.Component;

import javax.annotation.CheckReturnValue;
import javax.annotation.meta.When;
import java.util.UUID;

@Component
public class MdcUtility {

    public static final String USER_ID_HEADER = "userId";
    public static final String CORRELATION_ID_HEADER = "correlationId";

    @CheckReturnValue(when = When.NEVER)
    public String generateDefaultUserId() {
        return "Anonymous";
    }

    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

}
