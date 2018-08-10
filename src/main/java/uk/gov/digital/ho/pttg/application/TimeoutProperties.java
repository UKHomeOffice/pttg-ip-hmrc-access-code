package uk.gov.digital.ho.pttg.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "timeouts")
public class TimeoutProperties {

    private Audit audit;
    private Hmrc hmrc;

    public static class Audit {
        private int readMs;
        private int connectMs;

        public int getReadMs() {
            return readMs;
        }

        public void setReadMs(int readMs) {
            this.readMs = readMs;
        }

        public int getConnectMs() {
            return connectMs;
        }

        public void setConnectMs(int connectMs) {
            this.connectMs = connectMs;
        }
    }

    public static class Hmrc {
        private int readMs;
        private int connectMs;

        public int getReadMs() {
            return readMs;
        }

        public void setReadMs(int readMs) {
            this.readMs = readMs;
        }

        public int getConnectMs() {
            return connectMs;
        }

        public void setConnectMs(int connectMs) {
            this.connectMs = connectMs;
        }
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public Hmrc getHmrc() {
        return hmrc;
    }

    public void setHmrc(Hmrc hmrc) {
        this.hmrc = hmrc;
    }
}
