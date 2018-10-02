package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.digital.ho.pttg.audit.AuditClient;
import uk.gov.digital.ho.pttg.hmrc.AccessCodeHmrc;
import uk.gov.digital.ho.pttg.hmrc.HmrcClient;
import uk.gov.digital.ho.pttg.jpa.AccessCodeJpa;
import uk.gov.digital.ho.pttg.jpa.AccessRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AccessCodeResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessRepository accessRepository;

    @MockBean
    private HmrcClient hmrcClient;

    @MockBean
    private AuditClient auditClient;

    @MockBean
    private RequestData requestData;

    @Before
    public void setUp() {
        when(requestData.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any(Object.class))).thenReturn(true);
    }

    @Test
    public void shouldRefreshAccessCodeIfReported() throws Exception {
        AccessCodeJpa badAccessCode = new AccessCodeJpa(LocalDateTime.now(), LocalDateTime.now(), "bad-access-code");
        accessRepository.save(badAccessCode);

        AccessCodeHmrc newAccessCodeHmrc = new AccessCodeHmrc("good-access-code", Integer.MAX_VALUE, "refresh-token");
        when(hmrcClient.getAccessCodeFromHmrc(anyString())).thenReturn(newAccessCodeHmrc);

        mockMvc.perform(post("/access/bad-access-code/report"))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get("/access"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("good-access-code"));

        verify(hmrcClient).getAccessCodeFromHmrc(anyString());
    }

    @Test
    public void shouldNotRefreshAccessCodeIfOldCodeReported() throws Exception {
        AccessCodeJpa goodAccessCode = new AccessCodeJpa(LocalDateTime.now().plusDays(1l), LocalDateTime.now().plusDays(1l), "good-access-code");
        accessRepository.save(goodAccessCode);

        mockMvc.perform(post("/access/old-access-code/report"))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get("/access"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("good-access-code"));

        verifyZeroInteractions(hmrcClient);
    }
}
