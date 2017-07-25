package uk.gov.digital.ho.pttg.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccessCodeResourceTest {

    @Mock
    private AccessCode stubAccessCode;

    @Mock
    private AccessCodeGenerator mockAccessCodeGenerator;

    @InjectMocks
    private AccessCodeResource resource;

    @Test
    public void shouldUseCollaborators_getAccessCodeGenerator() throws IOException {

        when(mockAccessCodeGenerator.getAccessCode(any(LocalDateTime.class))).thenReturn(stubAccessCode);

        resource.getAccessCodeGenerator();

        verify(mockAccessCodeGenerator).getAccessCode(any(LocalDateTime.class));
    }

    @Test
    public void shouldReturnAccessCodeResponse() throws IOException {

        when(mockAccessCodeGenerator.getAccessCode(any(LocalDateTime.class))).thenReturn(stubAccessCode);

        ResponseEntity response = resource.getAccessCodeGenerator();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(((AccessCode)response.getBody())).isEqualTo(stubAccessCode);
    }
}