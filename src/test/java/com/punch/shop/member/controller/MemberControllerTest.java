package com.punch.shop.member.controller;

import com.punch.shop.member.dto.MemberProfileResponse;
import com.punch.shop.member.dto.MemberProfileUpdateRequest;
import com.punch.shop.member.dto.MemberRegisterRequest;
import com.punch.shop.member.dto.MemberRegisterResponse;
import com.punch.shop.member.exception.DuplicateEmailException;
import com.punch.shop.member.model.MemberPrincipal;
import com.punch.shop.member.service.CustomUserDetailsService;
import com.punch.shop.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private MemberPrincipal principal;
    private MemberProfileResponse profileResponse;

    @BeforeEach
    void setUp() {
        principal = new MemberPrincipal(1L, "test@example.com", "encodedPassword", List.of());
        profileResponse = MemberProfileResponse.builder()
                .email("test@example.com")
                .name("홍길동")
                .phone("010-1234-5678")
                .build();
    }

    @Test
    @DisplayName("로그인 폼 페이지 요청")
    void loginForm() throws Exception {
        mockMvc.perform(get("/member/login").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("member/login"));
    }

    @Test
    @DisplayName("회원가입 폼 페이지 요청")
    void registerForm() throws Exception {
        mockMvc.perform(get("/member/register").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("member/register"))
                .andExpect(model().attributeExists("memberRegisterRequest"));
    }

    @Test
    @DisplayName("회원가입 성공")
    void registerSuccess() throws Exception {
        MemberRegisterResponse response = MemberRegisterResponse.builder()
                .memberId(1L)
                .email("test@example.com")
                .name("홍길동")
                .createdAt(LocalDateTime.now())
                .build();

        given(memberService.register(any(MemberRegisterRequest.class))).willReturn(response);

        mockMvc.perform(post("/member/register").with(user(principal)).with(csrf())
                        .param("email", "test@example.com")
                        .param("password", "password123")
                        .param("name", "홍길동")
                        .param("phone", "010-1234-5678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("회원가입 실패 - validation 오류")
    void registerValidationFail() throws Exception {
        mockMvc.perform(post("/member/register").with(user(principal)).with(csrf())
                        .param("email", "invalid-email")
                        .param("password", "short")
                        .param("name", "")
                        .param("phone", "invalid-phone"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/register"))
                .andExpect(model().attributeHasFieldErrors("memberRegisterRequest", "email", "password", "name", "phone"));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void registerDuplicateEmail() throws Exception {
        given(memberService.register(any(MemberRegisterRequest.class))).willThrow(new DuplicateEmailException("이미 사용 중인 이메일입니다"));

        mockMvc.perform(post("/member/register").with(user(principal)).with(csrf())
                        .param("email", "test@example.com")
                        .param("password", "password123")
                        .param("name", "홍길동")
                        .param("phone", "010-1234-5678"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/register"))
                .andExpect(model().attributeHasFieldErrors("memberRegisterRequest", "email"));
    }

    @Test
    @DisplayName("프로필 페이지 요청")
    void profileForm() throws Exception {
        given(memberService.getProfile(1L)).willReturn(profileResponse);

        mockMvc.perform(get("/member/profile").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("member/profile"))
                .andExpect(model().attributeExists("profile", "memberProfileUpdateRequest"));
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void updateProfileSuccess() throws Exception {
        given(memberService.updateProfile(eq(1L), any(MemberProfileUpdateRequest.class))).willReturn(profileResponse);

        mockMvc.perform(post("/member/profile").with(user(principal)).with(csrf())
                        .param("name", "김철수")
                        .param("phone", "010-9876-5432"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/profile"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("프로필 수정 실패 - validation 오류")
    void updateProfileValidationFail() throws Exception {
        given(memberService.getProfile(1L)).willReturn(profileResponse);

        mockMvc.perform(post("/member/profile").with(user(principal)).with(csrf())
                        .param("name", "")
                        .param("phone", "invalid"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/profile"))
                .andExpect(model().attributeHasFieldErrors("memberProfileUpdateRequest", "name", "phone"));
    }
}
