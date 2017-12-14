package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.Base64;

@Component
@Slf4j
public class RequestData implements HandlerInterceptor {

    static final String SESSION_ID_HEADER = "x-session-id";
    static final String CORRELATION_ID_HEADER = "x-correlation-id";

    @Value("${auditing.deployment.name}") private String deploymentName;
    @Value("${auditing.deployment.namespace}") private String deploymentNamespace;
    @Value("${audit.service.auth}") private String auditBasicAuth;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        MDC.clear();
        MDC.put(SESSION_ID_HEADER, initialiseSessionId(request));
        MDC.put(CORRELATION_ID_HEADER, initialiseCorrelationId(request));

        log.debug("Added session id {} to MDC: {}", initialiseSessionId(request), sessionId());
        log.debug("Added correlation id {} to MDC: {}", initialiseCorrelationId(request), correlationId());

        return true;
    }

    private String initialiseSessionId(HttpServletRequest request) {
        String sessionId = WebUtils.getSessionId(request);
        return StringUtils.isNotBlank(sessionId) ? sessionId : "unknown";
    }

    private String initialiseCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        return StringUtils.isNotBlank(correlationId) ? correlationId : "unknown";
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        MDC.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

    public String deploymentName() {
        return deploymentName;
    }

    public String deploymentNamespace() {
        return deploymentNamespace;
    }

    public String auditBasicAuth() { return String.format("Basic %s", Base64.getEncoder().encodeToString(auditBasicAuth.getBytes(Charset.forName("UTF-8")))); }

    public String sessionId() {
        return MDC.get(SESSION_ID_HEADER);
    }

    public String correlationId() {
        return MDC.get(CORRELATION_ID_HEADER);
    }

}
