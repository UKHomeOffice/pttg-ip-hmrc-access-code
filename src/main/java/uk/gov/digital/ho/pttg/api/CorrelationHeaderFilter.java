package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

import static uk.gov.digital.ho.pttg.api.MdcUtility.CORRELATION_ID_HEADER;

@Slf4j
@Component
public class CorrelationHeaderFilter implements Filter {

    @Autowired
    private MdcUtility mdcUtility;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // interface method
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Optional<String> correlationIdOptional = Optional.ofNullable(((HttpServletRequest) request).getHeader(CORRELATION_ID_HEADER));

        try {
            MDC.put(CORRELATION_ID_HEADER, correlationIdOptional.orElseGet(mdcUtility::generateCorrelationId));
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_HEADER);
        }
    }

    @Override
    public void destroy() {
        // interface method
    }

}
