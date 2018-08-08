package uk.gov.digital.ho.pttg.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "timeouts")
public class TimeoutProperties {

    private Audit audit;
    private Hmrc hmrc;

    public static class Audit {
        private int readSeconds;
        private int connectSeconds;

        public int getReadSeconds() {
            return readSeconds;
        }

        public void setReadSeconds(int readSeconds) {
            this.readSeconds = readSeconds;
        }

        public int getConnectSeconds() {
            return connectSeconds;
        }

        public void setConnectSeconds(int connectSeconds) {
            this.connectSeconds = connectSeconds;
        }
    }

    public static class Hmrc {
        private int readSeconds;
        private int connectSeconds;

        public int getReadSeconds() {
            return readSeconds;
        }

        public void setReadSeconds(int readSeconds) {
            this.readSeconds = readSeconds;
        }

        public int getConnectSeconds() {
            return connectSeconds;
        }

        public void setConnectSeconds(int connectSeconds) {
            this.connectSeconds = connectSeconds;
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
