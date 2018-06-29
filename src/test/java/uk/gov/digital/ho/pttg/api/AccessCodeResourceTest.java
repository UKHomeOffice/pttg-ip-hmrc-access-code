package uk.gov.digital.ho.pttg.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessCodeResourceTest {

    @Mock
    private AccessCode stubAccessCode;

    @Mock
    private AccessCodeService mockAccessCodeService;

    @InjectMocks
    private AccessCodeResource resource;

    @Test
    public void shouldUseCollaborators_getAccessCode() throws IOException {

        when(mockAccessCodeService.getAccessCode()).thenReturn(stubAccessCode);

        resource.getAccessCode();

        verify(mockAccessCodeService).getAccessCode();
    }

    @Test
    public void shouldReturnAccessCodeResponse() throws IOException {

        when(mockAccessCodeService.getAccessCode()).thenReturn(stubAccessCode);

        ResponseEntity response = resource.getAccessCode();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((AccessCode)response.getBody())).isEqualTo(stubAccessCode);
    }

    @Test
    public void shouldUseCollaborators_refresh() throws IOException {

        resource.refresh();

        verify(mockAccessCodeService).refreshAccessCode();
    }
}