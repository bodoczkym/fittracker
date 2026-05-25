package com.bodoczky.fittracker.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end check that HTTP Basic auth (ADR-0001) actually gates the API: the full
 * security filter chain is wired, with the default dev in-memory user (admin/admin).
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void api_returns401_withoutCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/cycles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void api_returns401_withWrongCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/cycles").with(httpBasic("admin", "nope")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void api_returns200_withValidCredentials() throws Exception {
        mockMvc.perform(get("/api/v1/cycles").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());
    }
}
