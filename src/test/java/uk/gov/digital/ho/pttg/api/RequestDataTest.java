package uk.gov.digital.ho.pttg.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.api.RequestData.CORRELATION_ID_HEADER;
import static uk.gov.digital.ho.pttg.api.RequestData.SESSION_ID_HEADER;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RequestData.class})
@TestPropertySource(properties = {"auditing.deployment.name=some-name",
                                    "auditing.deployment.namespace=some-namespace",
                                    "audit.service.auth=some-auth"})
public class RequestDataTest {

    @Autowired
    private RequestData requestData;

    @Test
    public void shouldConfigureMDC_preHandle() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        requestData.preHandle(mockRequest, mockResponse, null);

        assertThat(MDC.get(SESSION_ID_HEADER)).isEqualTo("unknown");
        assertThat(MDC.get(CORRELATION_ID_HEADER)).isEqualTo("unknown");
    }

    @Test
    public void shouldResetMDC_postHandle() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        requestData.preHandle(request, response, null);
        requestData.postHandle(null, null, null, null);

        assertThat(MDC.get(SESSION_ID_HEADER)).isNull();
        assertThat(MDC.get(CORRELATION_ID_HEADER)).isNull();
    }

    @Test
    public void shouldExposeDeploymentName() {
        assertThat(requestData.deploymentName()).isEqualTo("some-name");
    }

    @Test
    public void shouldExposeDeploymentNameSpace() {
        assertThat(requestData.deploymentNamespace()).isEqualTo("some-namespace");
    }

    @Test
    public void shouldExposeBasicAuthHeaderValue() {
        assertThat(requestData.auditBasicAuth()).isEqualTo("Basic c29tZS1hdXRo");
    }

    @Test
    public void shouldExposeSessionId() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        requestData.preHandle(mockRequest, mockResponse, null);

        assertThat(requestData.sessionId()).isEqualTo("unknown");
    }

    @Test
    public void shouldExposeCorrelationId() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        when(mockRequest.getHeader(CORRELATION_ID_HEADER)).thenReturn("some correlation id");

        requestData.preHandle(mockRequest, mockResponse, null);

        assertThat(requestData.correlationId()).isEqualTo("some correlation id");
    }
}