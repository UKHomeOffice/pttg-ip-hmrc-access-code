package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.api.MdcUtility.CORRELATION_ID_HEADER;

@RunWith(MockitoJUnitRunner.class)
public class CorrelationHeaderFilterTest {

    @Mock
    private FilterChain mockFilterChain;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private ServletResponse mockServletResponse;

    @Mock
    private MdcUtility mockMdcUtility;

    @InjectMocks
    private CorrelationHeaderFilter correlationHeaderFilter;

    @Before
    public void setup() {
        MDC.clear();
    }

    @Test
    public void shouldUseCollaborators() throws IOException, ServletException {

        when(mockHttpServletRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(null);
        when(mockMdcUtility.generateCorrelationId()).thenReturn("never seen");

        correlationHeaderFilter.doFilter(mockHttpServletRequest, mockServletResponse, mockFilterChain);

        verify(mockFilterChain).doFilter(mockHttpServletRequest, mockServletResponse);
        verify(mockMdcUtility).generateCorrelationId();
    }

    @Test
    public void shouldAddSuppliedDataToMdcBeforeProceeding() throws IOException, ServletException {
        when(mockHttpServletRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn("supplied data should go in the MDC");

        assertThat(MDC.get(CORRELATION_ID_HEADER))
                .as("MDC needs to be empty before the test")
                .isEqualTo(null);

        FilterChain stubFilterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                assertThat(MDC.get(CORRELATION_ID_HEADER)).isEqualTo("supplied data should go in the MDC");
            }
        };

        correlationHeaderFilter.doFilter(mockHttpServletRequest, mockServletResponse, stubFilterChain);

        assertThat(MDC.get(CORRELATION_ID_HEADER))
                .as("MDC should be empty after the test")
                .isEqualTo(null);
    }

    @Test
    public void shouldAddDefaultDataToMdcBeforeProceeding() throws IOException, ServletException {
        when(mockHttpServletRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn(null);
        when(mockMdcUtility.generateCorrelationId()).thenReturn("default data should go in the MDC");

        assertThat(MDC.get(CORRELATION_ID_HEADER))
                .as("MDC needs to be empty before the test")
                .isEqualTo(null);

        FilterChain stubFilterChain = (request, response) -> assertThat(MDC.get(CORRELATION_ID_HEADER)).isEqualTo("default data should go in the MDC");

        correlationHeaderFilter.doFilter(mockHttpServletRequest, mockServletResponse, stubFilterChain);

        assertThat(MDC.get(CORRELATION_ID_HEADER))
                .as("MDC should be empty after the test")
                .isEqualTo(null);
    }

}