package com.punch.shop.member.service;

import com.punch.shop.member.dto.MemberProfileResponse;
import com.punch.shop.member.dto.MemberProfileUpdateRequest;
import com.punch.shop.member.dto.MemberRegisterRequest;
import com.punch.shop.member.dto.MemberRegisterResponse;
import com.punch.shop.member.exception.DuplicateEmailException;
import com.punch.shop.member.exception.MemberNotFoundException;
import com.punch.shop.member.model.Member;
import com.punch.shop.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 성공 - 이메일 중복 없을 경우")
    void registerSuccess() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("홍길동")
                .phone("010-1234-5678")
                .build();

        given(memberRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class)))
            .willReturn(Member.builder()
                .email(request.getEmail())
                .password("encodedPassword")
                .name(request.getName())
                .phone("01012345678")
                .build()
            );

        MemberRegisterResponse response = memberService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getName()).isEqualTo(request.getName());

        verify(passwordEncoder).encode(request.getPassword());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복 시 DuplicateEmailException 발생")
    void registerDuplicateEmail() {
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("홍길동")
                .phone("010-1234-5678")
                .build();

        given(memberRepository.existsByEmail(request.getEmail())).willReturn(true);

        assertThatThrownBy(() -> memberService.register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 사용 중인 이메일입니다");

        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("프로필 조회 성공")
    void getProfileSuccess() {
        String email = "test@example.com";
        Member member = Member.builder()
                .email(email)
                .password("encodedPassword")
                .name("홍길동")
                .phone("01012345678")
                .build();

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

        MemberProfileResponse response = memberService.getProfile(email);

        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getPhone()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("프로필 조회 실패 - 존재하지 않는 이메일이면 MemberNotFoundException 발생")
    void getProfileNotFound() {
        String email = "notfound@example.com";
        given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getProfile(email))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining(email);
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void updateProfileSuccess() {
        String email = "test@example.com";
        Member member = Member.builder()
                .email(email)
                .password("encodedPassword")
                .name("홍길동")
                .phone("01012345678")
                .build();
        MemberProfileUpdateRequest request = MemberProfileUpdateRequest.builder()
                .name("김철수")
                .phone("010-9876-5432")
                .build();

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));

        MemberProfileResponse response = memberService.updateProfile(email, request);

        assertThat(response.getName()).isEqualTo("김철수");
        assertThat(response.getPhone()).isEqualTo("010-9876-5432");
    }

    @Test
    @DisplayName("프로필 수정 실패 - 존재하지 않는 이메일이면 MemberNotFoundException 발생")
    void updateProfileNotFound() {
        String email = "notfound@example.com";
        MemberProfileUpdateRequest request = MemberProfileUpdateRequest.builder()
                .name("김철수")
                .phone("010-9876-5432")
                .build();

        given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.updateProfile(email, request))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining(email);
    }
}
