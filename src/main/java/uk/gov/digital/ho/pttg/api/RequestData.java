package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.Base64;

@Slf4j
public class RequestData implements HandlerInterceptor {

    public static final String SESSION_ID_HEADER = "x-session-id";
    public static final String CORRELATION_ID_HEADER = "x-correlation-id";
    public static final String USER_ID_HEADER = "x-auth-userid";

    @Value("${auditing.deployment.name}") private String deploymentName;
    @Value("${auditing.deployment.namespace}") private String deploymentNamespace;
    @Value("${audit.service.auth}") private String auditBasicAuth;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        MDC.clear();

        MDC.put(SESSION_ID_HEADER, initialiseSessionId(request));
        MDC.put(CORRELATION_ID_HEADER, initialiseCorrelationId(request));
        MDC.put(USER_ID_HEADER, initialiseUserName(request));

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        MDC.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        response.setHeader(SESSION_ID_HEADER, sessionId());
        response.setHeader(USER_ID_HEADER, userId());
        response.setHeader(CORRELATION_ID_HEADER, correlationId());
        MDC.clear();
    }

    private String initialiseSessionId(HttpServletRequest request) {
        String sessionId = WebUtils.getSessionId(request);
        return StringUtils.isNotBlank(sessionId) ? sessionId : "unknown";
    }

    private String initialiseCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        return StringUtils.isNotBlank(correlationId) ? correlationId : "unknown";
    }

    private String initialiseUserName(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        return StringUtils.isNotBlank(userId) ? userId : "anonymous";
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

    public String userId() {
        return MDC.get(USER_ID_HEADER);
    }
}
