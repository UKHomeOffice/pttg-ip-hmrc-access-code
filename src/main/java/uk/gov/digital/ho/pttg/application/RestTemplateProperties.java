package uk.gov.digital.ho.pttg.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "resttemplate")
public class RestTemplateProperties {

    private Audit audit;
    private Hmrc hmrc;

    public static class Audit {
        private int readTimeout;
        private int connectTimeout;

        public int getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }
    }

    public static class Hmrc {
        private int readTimeout;
        private int connectTimeout;

        public int getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
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
