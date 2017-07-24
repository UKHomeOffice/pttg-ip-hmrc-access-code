package uk.gov.digital.ho.pttg.application;

public interface ApplicationExceptions {

    class HmrcAccessCodeServiceRuntimeException extends RuntimeException {
        public HmrcAccessCodeServiceRuntimeException(Throwable cause) {
            super(cause);
        }
    }
}
