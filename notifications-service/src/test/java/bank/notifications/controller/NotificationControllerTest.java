package bank.notifications.controller;

import bank.notifications.config.SecurityConfig;
import bank.notifications.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class,
        properties = "spring.config.import=optional:consul:")
@Import(SecurityConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void notify_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/internal/notifications")
                        .contentType("application/json")
                        .content("{\"recipientLogin\": \"ivan\", \"message\": \"test\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void notify_withoutScope_returns403() throws Exception {
        mockMvc.perform(post("/internal/notifications")
                        .contentType("application/json")
                        .content("{\"recipientLogin\": \"ivan\", \"message\": \"test\"}")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "ivan"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_other"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void notify_withScope_returnsOk() throws Exception {
        mockMvc.perform(post("/internal/notifications")
                        .contentType("application/json")
                        .content("{\"recipientLogin\": \"ivan\", \"message\": \"test\"}")
                        .with(jwt().jwt(j -> j.claim("preferred_username", "ivan"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_notifications.write"))))
                .andExpect(status().isOk());
    }
}
