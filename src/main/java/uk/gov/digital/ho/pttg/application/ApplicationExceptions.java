package uk.gov.digital.ho.pttg.application;

public interface ApplicationExceptions {

    class HmrcAccessCodeServiceRuntimeException extends RuntimeException {

        public HmrcAccessCodeServiceRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }

        public HmrcAccessCodeServiceRuntimeException(String message) {
            super(message);
        }
    }

    class HmrcRetrieveAccessCodeException extends RuntimeException {
        public HmrcRetrieveAccessCodeException(String message) {
            super(message);
        }
    }

    class HmrcUnauthorisedException extends RuntimeException {
        public HmrcUnauthorisedException(final String s) {
            super(s);
        }

        public HmrcUnauthorisedException(final String s, final Exception e) {
            super(s, e);
        }
    }

    class ProxyForbiddenException extends RuntimeException {
        public ProxyForbiddenException(String s) {
            super(s);
        }
    }

}
