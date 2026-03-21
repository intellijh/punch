package com.punch.shop.common.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("PasswordEncoder 빈이 BCryptPasswordEncoder로 등록되어 있는지 확인")
    void passwordEncoderBean() {
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    @DisplayName("Public URL은 인증 없이 접근 가능")
    void publicUrlAccessWithoutAuth() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/member/register"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/member/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/error는 인증 없이 접근 가능 (로그인 페이지로 redirect 없음)")
    void errorUrlAccessWithoutAuth() throws Exception {
        mockMvc.perform(get("/error"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Protected URL은 인증 없이 접근 시 로그인 페이지로 redirect")
    void protectedUrlRedirectToLogin() throws Exception {
        mockMvc.perform(get("/member/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/member/login"));
    }

    @Test
    @DisplayName("로그아웃 시 세션 무효화 및 쿠키 삭제")
    void logoutTest() throws Exception {
        mockMvc.perform(post("/member/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
