package uk.gov.digital.ho.pttg.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "resttemplate")
public class RestTemplateProperties {

    private Audit audit;
    private Hmrc hmrc;
    private boolean proxyEnabled;
    private String hmrcBaseUrl;
    private String proxyHost;
    private Integer proxyPort;

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

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public void setProxyEnabled(boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }

    public String getHmrcBaseUrl() {
        return hmrcBaseUrl;
    }

    public void setHmrcBaseUrl(String hmrcBaseUrl) {
        this.hmrcBaseUrl = hmrcBaseUrl;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }
}
