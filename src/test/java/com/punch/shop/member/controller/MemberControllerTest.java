package com.punch.shop.member.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.punch.shop.member.dto.MemberRegisterResponse;
import com.punch.shop.member.exception.DuplicateEmailException;
import com.punch.shop.member.service.MemberService;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 폼 페이지 요청")
    void registerForm() throws Exception {
        mockMvc.perform(get("/member/register"))
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

        given(memberService.register(any())).willReturn(response);

        mockMvc.perform(post("/member/register")
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
        mockMvc.perform(post("/member/register")
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
        given(memberService.register(any())).willThrow(new DuplicateEmailException("이미 사용 중인 이메일입니다"));

        mockMvc.perform(post("/member/register")
                        .param("email", "test@example.com")
                        .param("password", "password123")
                        .param("name", "홍길동")
                        .param("phone", "010-1234-5678"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/register"))
                .andExpect(model().attributeHasFieldErrors("memberRegisterRequest", "email"));
    }
}
